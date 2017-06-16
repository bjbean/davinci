/*
 * Bar chart options generator
 */

export default function (dataSource, flatInfo, chartParams) {
  const {
    type
  } = flatInfo

  const {
    xAxis,
    metrics,
    stack,
    label,
    tooltip,
    legend,
    toolbox
  } = chartParams

  let metricOptions,
    xAxisOptions,
    stackOption,
    labelOption,
    tooltipOptions,
    legendOptions,
    toolboxOptions,
    gridOptions

  // series 数据项
  let metricArr = []

  if (metrics) {
    metrics.forEach(m => {
      stackOption = stack && stack.length ? { stack: 'stack' } : null
      labelOption = label && label.length
        ? {
          label: {
            normal: {
              show: true,
              position: stack && stack.length ? 'insideTop' : 'top'
            }
          }
        } : null

      let serieObj = Object.assign({},
        {
          name: m,
          type: type,
          sampling: 'average',
          data: dataSource.map(d => d[m])
        },
        stackOption,
        labelOption
      )
      metricArr.push(serieObj)
    })
    metricOptions = {
      series: metricArr
    }
  }

  // x轴数据
  xAxisOptions = xAxis && {
    xAxis: {
      data: dataSource.map(d => d[xAxis])
    }
  }

  // tooltip
  tooltipOptions = tooltip && tooltip.length
    ? {
      tooltip: {
        trigger: 'axis'
      }
    } : null

  // legend
  legendOptions = legend && legend.length
    ? {
      legend: {
        data: metricArr.map(m => m.name),
        align: 'left',
        right: 200
      }
    } : null

  // toolbox
  toolboxOptions = toolbox && toolbox.length
    ? {
      toolbox: {
        feature: {
          dataZoom: {
            yAxisIndex: 'none'
          },
          restore: {},
          saveAsImage: {
            pixelRatio: 2
          }
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
      yAxis: {
        type: 'value'
      }
    },
    metricOptions,
    xAxisOptions,
    tooltipOptions,
    legendOptions,
    toolboxOptions,
    gridOptions
  )
}
