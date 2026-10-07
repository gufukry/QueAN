// QA-only: inspect exactly one PID/creation identity. No signals, focus,
// permission requests, process enumeration, debugger attachment or VM writes.
import AppKit
import CoreGraphics
import Darwin

func emit(_ value: [String: Any]) {
    do {
        let bytes = try JSONSerialization.data(withJSONObject: value, options: [.sortedKeys])
        FileHandle.standardOutput.write(bytes)
        FileHandle.standardOutput.write(Data([10]))
    } catch { fputs("owned game observer JSON error\n", stderr); exit(2) }
}
func identity(_ pid: Int32) -> [String: Any]? {
    var info = proc_bsdinfo()
    let size = Int32(MemoryLayout<proc_bsdinfo>.size)
    guard proc_pidinfo(pid, PROC_PIDTBSDINFO, 0, &info, size) == size else { return nil }
    var bytes = [CChar](repeating: 0, count: 4 * Int(MAXPATHLEN))
    let byteCount = UInt32(bytes.count)
    guard proc_pidpath(pid, &bytes, byteCount) > 0 else { return nil }
    return ["pid": pid, "ppid": info.pbi_ppid,
            "creationUnixUS": String(info.pbi_start_tvsec * 1_000_000 + info.pbi_start_tvusec),
            "executable": String(cString: bytes)]
}
let args = Array(CommandLine.arguments.dropFirst())
guard args.count >= 2, ["--identity", "--observe"].contains(args[0]),
      let pid = Int32(args[1]), pid > 0 else {
    emit(["complete": false, "error": "exact PID required"]); exit(2)
}
let started = Date().timeIntervalSince1970 * 1000
guard let before = identity(pid) else {
    emit(["complete": false, "pid": pid, "errno": errno,
          "error": "owned process identity unavailable", "startedAtUnixMs": started]); exit(3)
}
if args[0] == "--identity" { emit(before); exit(0) }
guard args.count == 4, before["creationUnixUS"] as? String == args[2],
      before["executable"] as? String == args[3] else {
    emit(["complete": false, "before": before, "error": "PID creation/executable mismatch"]); exit(4)
}
let onScreen = CGWindowListCopyWindowInfo([.optionOnScreenOnly, .excludeDesktopElements], kCGNullWindowID) as? [[String: Any]]
let all = CGWindowListCopyWindowInfo([.optionAll, .excludeDesktopElements], kCGNullWindowID) as? [[String: Any]]
let visibleIDs = Set((onScreen ?? []).compactMap { $0[kCGWindowNumber as String] as? UInt32 })
// CoreGraphics provides a window list; publish only the exact owned PID rows.
let windows: [[String: Any]] = (all ?? []).compactMap { item in
    guard let owner = item[kCGWindowOwnerPID as String] as? Int32, owner == pid,
          let id = item[kCGWindowNumber as String] as? UInt32 else { return nil }
    return ["id": id, "ownerPID": owner, "onScreen": visibleIDs.contains(id),
            "name": item[kCGWindowName as String] as? String ?? "",
            "layer": item[kCGWindowLayer as String] as? Int ?? -1,
            "bounds": item[kCGWindowBounds as String] ?? [:],
            "alpha": item[kCGWindowAlpha as String] as? Double ?? -1]
}
let after = identity(pid)
let stable = after?["creationUnixUS"] as? String == args[2] && after?["executable"] as? String == args[3]
emit(["complete": stable && all != nil && onScreen != nil, "before": before,
      "after": after as Any? ?? NSNull(), "identityStable": stable,
      "startedAtUnixMs": started, "finishedAtUnixMs": Date().timeIntervalSince1970 * 1000,
      "allWindowListAvailable": all != nil, "onScreenWindowListAvailable": onScreen != nil,
      "windows": windows, "screenCaptureAccess": CGPreflightScreenCaptureAccess(),
      "permissionNote": "preflight only; no prompt, no screen capture, no focus change"])
exit(stable ? 0 : 4)
