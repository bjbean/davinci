export const env = 'production'

export default {
  dev: {
    host: 'http://localhost:3111'
  },
  production: {
    // host: 'http://10.10.38.39:8080/api/v1'
    // host: 'http://10.143.129.32:9080/api/v1',
    // shareHost: 'http://10.143.129.32:9080/share'
    host: 'http://edp.davinci.creditease.corp/api/v1',
    shareHost: 'http://edp.davinci.creditease.corp/share'
    // host: 'http://10.141.5.91:9080/api/v1',  // 宜人贷数据部数据挖掘组
    // shareHost: 'http://10.141.5.91:9080/share'
    // host: 'http://10.141.5.50:9080/api/v1', // 宜人贷数据部数据科学组
    // shareHost: 'http://10.141.5.50:9080/share'
    // host: 'http://10.143.129.32:9081/api/v1', // 志诚阿福
    // shareHost: 'http://10.143.129.32:9081/share',
    // host: 'http://10.143.129.136:9080/api/v1',  // 王贺
    // shareHost: 'http://10.143.129.136:9080/share'
  }
}
