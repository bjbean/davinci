export const env = 'production'

export default {
  dev: {
    host: 'http://localhost:3111',
    shareHost: 'http://localhost:3111'
  },
  production: {
    host: window._DAVINCI_PRODUCTION_HOST_,
    shareHost: window._DAVINCI_PRODUCTION_SHARE_HOST_
    // host: 'http://10.143.129.32:9080/api/v1',
    // shareHost: 'http://10.143.129.32:9080/share'
    // host: 'http://edp.davinci.creditease.corp/api/v1',
    // shareHost: 'http://edp.davinci.creditease.corp/share'
  },
  echarts: {
    theme: {
      default: {
        color: ['#F44336', '#673AB7', '#03A9F4', '#4CAF50', '#FFC107', '#FF5722', '#607D8B', '#E91E63', '#3F51B5', '#00BCD4', '#8BC34A', '#FFEB3B', '#795548', '#000000', '#9C27B0', '#2196F3', '#009688', '#CDDC39', '#FF9800', '#9E9E9E'],
        backgroundColor: '#fff',
        graph: {
          color: ['#F44336', '#673AB7', '#03A9F4', '#4CAF50', '#FFC107', '#FF5722', '#607D8B', '#E91E63', '#3F51B5', '#00BCD4', '#8BC34A', '#FFEB3B', '#795548', '#000000', '#9C27B0', '#2196F3', '#009688', '#CDDC39', '#FF9800', '#9E9E9E']
        }
      }
    }
  }
}
