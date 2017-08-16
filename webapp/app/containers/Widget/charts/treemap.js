/*
 * Pie chart options generator
 */

export default function (dataSource, flatInfo, chartParams) {
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
    type: 'treemap',
    visibleMin: 300,
    label: {
      show: true,
      formatter: '{b}'
    },
    itemStyle: {
      normal: {
        borderColor: '#fff'
      }
    },
    levels: [
      {
        itemStyle: {
          normal: {
            borderWidth: 0,
            gapWidth: 5
          }
        }
      },
      {
        itemStyle: {
          normal: {
            gapWidth: 1
          }
        }
      },
      {
        colorSaturation: [0.35, 0.5],
        itemStyle: {
          normal: {
            gapWidth: 1,
            borderColorSaturation: 0.6
          }
        }
      }
    ],
    data: dataSource
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
