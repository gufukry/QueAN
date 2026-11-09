import os
import struct as st
import subprocess as sp
import zipfile
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent
if (ROOT / 'build.gradle.kts').exists():
    import shutil
    gradle = os.environ.get('GRADLE_BIN') or shutil.which('gradle')
    if not gradle:
        candidates = sorted(Path('/root/tools/gradle').glob('gradle-*/bin/gradle'))
        gradle = str(candidates[-1]) if candidates else None
    if not gradle:
        raise SystemExit('Compose 构建需要 Gradle，请安装后设置 GRADLE_BIN。')
    env = os.environ.copy()
    jdk = Path('/usr/lib/jvm/java-17-openjdk')
    if jdk.exists():
        env['JAVA_HOME'] = str(jdk)
    sp.run([gradle, '--no-daemon', 'assembleRelease'], cwd=ROOT, env=env, check=True)
    apk = ROOT / 'build/outputs/apk/release/sky-release.apk'
    with zipfile.ZipFile(apk) as package:
        assert package.testzip() is None, 'APK 文件损坏'
        assert {'AndroidManifest.xml', 'classes.dex', 'resources.arsc'}.issubset(package.namelist()), 'APK 缺少必要文件'
    java = str(Path(env['JAVA_HOME']) / 'bin/java') if 'JAVA_HOME' in env else 'java'
    tool_root = Path(os.environ.get('ANDROID_TOOL_ROOT', '/root/tools/android'))
    sp.run([java, '-jar', str(tool_root / 'build-tools/37.0.0/lib/apksigner.jar'), 'verify', '--verbose', str(apk)], check=True)
    print('APK:', apk, 'bytes:', apk.stat().st_size)
    raise SystemExit(0)
SDK = Path(os.environ.get('ANDROID_TOOL_ROOT', '/root/tools/android'))
PLATFORM = SDK / 'android-35/android.jar'
TOOLS = SDK / 'android-15/lib'
BUILD = ROOT / 'build'
ANDROID = 'http://schemas.android.com/apk/res/android'
IDS = {'versionCode': 0x0101021b, 'versionName': 0x0101021c,
       'minSdkVersion': 0x0101020c, 'targetSdkVersion': 0x01010270,
       'label': 0x01010001, 'allowBackup': 0x01010280, 'supportsRtl': 0x010103af,
       'name': 0x01010003, 'exported': 0x01010010, 'windowSoftInputMode': 0x0101022b}
NONE = 0xffffffff

def chunk(kind, header_size, body):
    return st.pack('<HHI', kind, header_size, 8 + len(body)) + body

def manifest():
    root = ET.parse(ROOT / 'AndroidManifest.xml').getroot()
    strings = list(IDS)
    def index(s):
        if s not in strings:
            strings.append(s)
        return strings.index(s)
    index('android'); index(ANDROID)
    for element in root.iter():
        index(element.tag)
        for key, value in element.attrib.items():
            index(key.split('}')[-1]); index(value)
    data = bytearray(); offsets = []
    for s in strings:
        offsets.append(len(data)); encoded = s.encode('utf-16le')
        data += st.pack('<H', len(encoded) // 2) + encoded + b'\0\0'
    data += b'\0' * (-len(data) % 4)
    pool = chunk(1, 28, st.pack('<IIIII', len(strings), 0, 0, 28 + 4 * len(strings), 0) + st.pack('<' + 'I' * len(offsets), *offsets) + data)
    mapping = chunk(0x180, 8, st.pack('<' + 'I' * len(IDS), *IDS.values()))
    def node(kind, body):
        return chunk(kind, 16, st.pack('<II', 1, NONE) + body)
    namespace = st.pack('<II', index('android'), index(ANDROID))
    def emit(e):
        attributes = bytearray()
        for key, value in e.attrib.items():
            name = key.split('}')[-1]
            ns = index(ANDROID) if key.startswith('{') else NONE
            kind, val = 3, index(value)
            if name in ('versionCode', 'minSdkVersion', 'targetSdkVersion'):
                kind, val = 0x10, int(value)
            elif value in ('true', 'false'):
                kind, val = 0x12, int(value == 'true')
            elif name == 'windowSoftInputMode':
                kind, val = 0x11, 0x10
            attributes += st.pack('<IIIHBBI', ns, index(name), index(value), 8, 0, kind, val)
        result = node(0x102, st.pack('<IIHHHHHH', NONE, index(e.tag), 20, 20, len(e.attrib), 0, 0, 0) + attributes)
        for child in e:
            result += emit(child)
        return result + node(0x103, st.pack('<II', NONE, index(e.tag)))
    content = pool + mapping + node(0x100, namespace) + emit(root) + node(0x101, namespace)
    return chunk(3, 8, content)

def run(*args):
    sp.run([str(a) for a in args], check=True)

BUILD.mkdir(exist_ok=True)
(BUILD / 'classes').mkdir(exist_ok=True)
run('javac', '-encoding', 'UTF-8', '-source', '8', '-target', '8', '-classpath', PLATFORM,
    '-d', BUILD / 'classes', *sorted((ROOT / 'src').rglob('*.java')))
run('jar', 'cf', BUILD / 'classes.jar', '-C', BUILD / 'classes', '.')
run('java', '-cp', TOOLS / 'd8.jar', 'com.android.tools.r8.D8', '--min-api', '26',
    '--lib', PLATFORM, '--output', BUILD, BUILD / 'classes.jar')
with zipfile.ZipFile(BUILD / 'unsigned.apk', 'w', zipfile.ZIP_DEFLATED) as apk:
    apk.writestr('AndroidManifest.xml', manifest())
    apk.write(BUILD / 'classes.dex', 'classes.dex')
    for asset in sorted((ROOT / 'assets').rglob('*')):
        if asset.is_file():
            apk.write(asset, 'assets/' + str(asset.relative_to(ROOT / 'assets')))
key = BUILD / 'debug.p12'
if not key.exists():
    run('keytool', '-genkeypair', '-keystore', key, '-storetype', 'PKCS12', '-storepass', 'android',
        '-keypass', 'android', '-alias', 'debug', '-keyalg', 'RSA', '-keysize', '2048',
        '-validity', '10000', '-dname', 'CN=Local Android Debug')
run('java', '-jar', TOOLS / 'apksigner.jar', 'sign', '--ks', key, '--ks-pass', 'pass:android',
    '--key-pass', 'pass:android', '--out', BUILD / 'terminal-launcher.apk', BUILD / 'unsigned.apk')
run('java', '-jar', TOOLS / 'apksigner.jar', 'verify', '--verbose', BUILD / 'terminal-launcher.apk')
with zipfile.ZipFile(BUILD / 'terminal-launcher.apk') as apk:
    assert apk.testzip() is None
    assert {'AndroidManifest.xml', 'classes.dex', 'assets/launcher.png'}.issubset(apk.namelist())
print('APK:', BUILD / 'terminal-launcher.apk')
