/*
 * Funnel chart options generator
 */

export default function (dataSource, flatInfo, chartParams) {
  const {
    type
  } = flatInfo

  const {
    title,
    value,
    min,
    max,
    gap,
    tooltip,
    legend,
    toolbox
  } = chartParams

  let metricOptions,
    minOption,
    maxOption,
    gapOption,
    tooltipOptions,
    legendOptions,
    toolboxOptions,
    gridOptions

  // series 数据项
  let metricArr = []

  minOption = min && {
    min: min
  }

  maxOption = max && {
    max: max
  }

  gapOption = gap && {
    gap: gap
  }

  let serieObj = Object.assign({},
    {
      name: '数据',
      type: type,
      left: '10%',
      width: '80%',
      minSize: '0%',
      maxSize: '100%',
      sort: 'descending',
      label: {
        normal: {
          show: true,
          position: 'inside'
        },
        emphasis: {
          textStyle: {
            fontSize: 20
          }
        }
      },
      labelLine: {
        normal: {
          length: 10,
          lineStyle: {
            width: 1,
            type: 'solid'
          }
        }
      },
      itemStyle: {
        normal: {
          borderColor: '#fff',
          borderWidth: 1
        }
      },
      data: dataSource.map(d => ({
        name: d[title],
        value: d[value]
      }))
    },
    minOption,
    maxOption,
    gapOption
  )
  metricArr.push(serieObj)
  metricOptions = {
    series: metricArr
  }

  // tooltip
  tooltipOptions = tooltip && tooltip.length
    ? {
      tooltip: {
        trigger: 'item'
      }
    } : null

  // legend
  legendOptions = legend && legend.length
    ? {
      legend: {
        data: dataSource.map(d => d.name),
        align: 'left'
      }
    } : null

  // toolbox
  toolboxOptions = toolbox && toolbox.length
    ? {
      toolbox: {
        feature: {
          dataView: {readOnly: false},
          restore: {},
          saveAsImage: {}
        }
      }
    } : null

  // grid
  gridOptions = {
    grid: {
      top: legend && legend.length  // FIXME
        ? Math.ceil(metricArr.length / Math.round((document.documentElement.clientWidth - 40 - 320 - 32 - 200) / 100)) * 30 + 10
        : 40,
      left: 60,
      right: 60,
      bottom: 30
    }
  }

  return Object.assign({},
    {
      calculable: true
    },
    metricOptions,
    tooltipOptions,
    legendOptions,
    toolboxOptions,
    gridOptions
  )
}
