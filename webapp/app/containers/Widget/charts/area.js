/*
 * Area chart options generator
 */
export default function (dataSource, flatInfo, chartParams) {
  const hasGroups = flatInfo.groups

  const {
    xAxis,
    metrics,
    groups,
    smooth,
    step,
    stack,
    symbol,
    tooltip,
    legend,
    toolbox
  } = chartParams

  let grouped,
    metricOptions,
    xAxisOptions,
    smoothOption,
    stepOption,
    stackOption,
    symbolOption,
    tooltipOptions,
    legendOptions,
    toolboxOptions,
    gridOptions

  // symbol
  symbolOption = symbol && symbol.length
    ? { symbol: 'emptyCircle' }
    : { symbol: 'none' }
  // smooth
  smoothOption = smooth && smooth.length ? { smooth: true } : null
  // step
  stepOption = step && step.length ? { step: true } : null
  // stack
  stackOption = stack && stack.length ? { stack: 'stack' } : null

  // 数据分组
  if (hasGroups && groups && groups.length) {
    grouped = makeGourped(dataSource, [].concat(groups).filter(i => !!i))
  }

  // series 数据项； series = metrics * groups
  let metricArr = []

  if (metrics) {
    metrics.forEach(m => {
      if (hasGroups && groups && groups.length) {
        Object
          .keys(grouped)
          .forEach(k => {
            let serieObj = Object.assign({},
              {
                name: `${k} ${m}`,
                type: 'line',
                areaStyle: {normal: {}},
                sampling: 'average',
                data: grouped[k].map(g => g[m])
              },
              symbolOption,
              smoothOption,
              stepOption,
              stackOption
            )
            metricArr.push(serieObj)
          })
      } else {
        let serieObj = Object.assign({},
          {
            name: m,
            type: 'line',
            areaStyle: {normal: {}},
            sampling: 'average',
            symbol: symbolOption,
            data: dataSource.map(d => d[m])
          },
          symbolOption,
          smoothOption,
          stepOption
        )
        metricArr.push(serieObj)
      }
    })
    metricOptions = {
      series: metricArr
    }
  }

  // x轴数据
  xAxisOptions = xAxis && {
    xAxis: {
      data: hasGroups && groups && groups.length
        ? Object.keys(grouped)
          .map(k => grouped[k])
          .reduce((longest, g) => longest.length > g.length ? longest : g, [])
          .map(item => item[xAxis])
        : dataSource.map(d => d[xAxis])
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

export function makeGourped (dataSource, groupColumns) {
  return dataSource.reduce((acc, val) => {
    let accColumn = groupColumns
      .reduce((arr, col) => {
        arr.push(val[col])
        return arr
      }, [])
      .join(' ')
    if (!acc[accColumn]) {
      acc[accColumn] = []
    }
    acc[accColumn].push(val)
    return acc
  }, {})
}
