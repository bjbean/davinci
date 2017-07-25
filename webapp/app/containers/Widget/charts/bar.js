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
    vertical,
    stack,
    label,
    tooltip,
    legend,
    toolbox,
    top,
    bottom,
    left,
    right
  } = chartParams

  let metricOptions,
    xAxisOptions,
    yAxisOptions,
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

      if (vertical && vertical.length) {
        labelOption = {
          label: {
            normal: {
              show: true,
              position: 'insideLeft'
            }
          }
        }
      } else {
        labelOption = label && label.length
          ? {
            label: {
              normal: {
                show: true,
                position: stack && stack.length ? 'insideTop' : 'top'
              }
            }
          } : null
      }

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

  // x轴与y轴数据
  if (vertical && vertical.length) {
    if (xAxis) {
      xAxisOptions = {
        yAxis: {
          data: dataSource.map(d => d[xAxis]),
          axisLabel: {
            show: false
          },
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          }
        }
      }
    }

    yAxisOptions = {
      xAxis: {
        type: 'value',
        position: 'top',
        splitLine: {
          lineStyle: {
            type: 'dashed'
          }
        }
      }
    }
  } else {
    if (xAxis) {
      xAxisOptions = {
        xAxis: {
          data: dataSource.map(d => d[xAxis]),
          axisLabel: {
            interval: 0,
            rotate: 45
          }
        }
      }
    }

    yAxisOptions = {
      yAxis: {
        type: 'value'
      }
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
      top: top,
      left: left,
      right: right,
      bottom: bottom
    }
  }

  return Object.assign({},
    metricOptions,
    xAxisOptions,
    yAxisOptions,
    tooltipOptions,
    legendOptions,
    toolboxOptions,
    gridOptions
  )
}
