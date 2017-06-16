import line from './charts/line'
import bar from './charts/bar'
import scatter from './charts/scatter'
import pie from './charts/pie'
import area from './charts/area'
import funnel from './charts/funnel'
import sankey from './charts/sankey'
import treemap from './charts/treemap'
import wordCloud from './charts/wordCloud'

export default function ({ dataSource, chartInfo, chartParams }) {
  // info 去层级
  const parsedParams = JSON.parse(chartInfo.params)
  const flatInfo = Object.assign({}, {
    name: chartInfo.name,
    type: chartInfo.type
  }, parsedParams.reduce((fi, info) => {
    info.items.forEach(i => {
      fi[i.name] = i
    })
    return fi
  }, {}))

  switch (flatInfo.type) {
    case 'line':
      return line(dataSource, flatInfo, chartParams)
    case 'bar':
      return bar(dataSource, flatInfo, chartParams)
    case 'scatter':
      return scatter(dataSource, flatInfo, chartParams)
    case 'pie':
      return pie(dataSource, flatInfo, chartParams)
    case 'area':
      return area(dataSource, flatInfo, chartParams)
    case 'sankey':
      return sankey(dataSource, flatInfo, chartParams)
    case 'funnel':
      return funnel(dataSource, flatInfo, chartParams)
    case 'treemap':
      return treemap(dataSource, flatInfo, chartParams)
    case 'wordCloud':
      return wordCloud(dataSource, flatInfo, chartParams)
    default:
      return {}
  }
}
