/*
 * Pie chart options generator
 */

export default function (dataSource, flatInfo, chartParams) {
  const {
    type
  } = flatInfo

  const {
    tooltip,
    toolbox
  } = chartParams

  let metricOptions,
    tooltipOptions,
    toolboxOptions,
    gridOptions

  // series 数据项
  let metricArr = []

  let serieObj = {
    name: '数据',
    type: type,
    layout: 'none',
    data: dataSource.nodes,
    links: dataSource.links,
    itemStyle: {
      normal: {
        borderWidth: 1,
        borderColor: '#aaa'
      }
    },
    lineStyle: {
      normal: {
        curveness: 0.5
      }
    }
  }
  metricArr.push(serieObj)
  metricOptions = {
    series: metricArr
  }

  // tooltip
  tooltipOptions = tooltip && tooltip.length
    ? {
      tooltip: {
        trigger: 'item',
        triggerOn: 'mousemove'
      }
    } : null

  // toolbox
  toolboxOptions = toolbox && toolbox.length
    ? {
      toolbox: {
        feature: {
          saveAsImage: {
            pixelRatio: 2
          }
        }
      }
    } : null

  // grid
  gridOptions = {
    grid: {
      top: 40,
      left: 60,
      right: 60,
      bottom: 30
    }
  }

  return Object.assign({},
    metricOptions,
    tooltipOptions,
    toolboxOptions,
    gridOptions
  )
}
