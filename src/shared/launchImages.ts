/** 内置启动插画已全部移除；保留类型与判定函数以维持调用方稳定。 */
export const BUILTIN_LAUNCH_IMAGES: ReadonlyArray<{ key: string; title: string; file: string }> = []

export function isBuiltinLaunchImage(key: string): boolean {
  return BUILTIN_LAUNCH_IMAGES.some(image => image.key === key)
}
