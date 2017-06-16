import notification from 'antd/lib/notification'

/**
 * UUID生成器
 * @param len 长度 number
 * @param radix 随机数基数 number
 * @returns {string}
 */
export const uuid = (len, radix) => {
  var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('')
  var uuid = []
  var i
  radix = radix || chars.length

  if (len) {
    // Compact form
    for (i = 0; i < len; i++) uuid[i] = chars[0 | Math.random() * radix]
  } else {
    // rfc4122, version 4 form
    var r

    // rfc4122 requires these characters
    uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-'
    uuid[14] = '4'

    // Fill in random data.  At i==19 set the high bits of clock sequence as
    // per rfc4122, sec. 4.1.5
    for (i = 0; i < 36; i++) {
      if (!uuid[i]) {
        r = 0 | Math.random() * 16
        uuid[i] = chars[(i === 19) ? (r & 0x3) | 0x8 : r]
      }
    }
  }
  return uuid.join('')
}

/**
 * 异常通知弹窗
 * @param err 异常内容: Error
 * @param title 弹窗标题: string
 */
export function notifyError (err, title) {
  notification.error({
    message: title,
    description: err.toString(),
    duration: null
  })
}

/**
 * sagas 异常通知
 * @param err 异常内容: Error
 * @param prefix sagas名称: string
 */
export function notifySagasError (err, prefix) {
  notifyError(err, `${prefix} sagas or reducer 异常`)
}
