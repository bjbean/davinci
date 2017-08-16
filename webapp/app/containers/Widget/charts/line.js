/*
 * Line chart options generator
 */
export default function (dataSource, flatInfo, chartParams) {
  const hasGroups = flatInfo.groups

  const {
    xAxis,
    metrics,
    groups,
    xAxisInterval,
    xAxisRotate,
    smooth,
    step,
    symbol,
    tooltip,
    legend,
    toolbox,
    top,
    bottom,
    left,
    right
  } = chartParams

  let grouped,
    metricOptions,
    xAxisOptions,
    smoothOption,
    stepOption,
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
                sampling: 'average',
                data: grouped[k].map(g => g[m])
              },
              symbolOption,
              smoothOption,
              stepOption
            )
            metricArr.push(serieObj)
          })
      } else {
        let serieObj = Object.assign({},
          {
            name: m,
            type: 'line',
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
        : dataSource.map(d => d[xAxis]),
      axisLabel: {
        interval: xAxisInterval,
        rotate: xAxisRotate
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
        top: 3,
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
        },
        right: 22
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
