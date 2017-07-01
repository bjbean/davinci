/*
 * Scatter chart options generator
 */

export default function (dataSource, flatInfo, chartParams) {
  const {
    type
  } = flatInfo

  const hasGroups = flatInfo.groups

  const {
    groups,
    xAxis,
    yAxis,
    splitLine,
    size,
    label,
    shadow,
    legend,
    toolbox
  } = chartParams

  let grouped,
    metricOptions,
    xAxisOptions,
    yAxisOptions,
    splitLineOptions,
    sizeOptions,
    labelOptions,
    shadowOptions,
    legendOptions,
    toolboxOptions,
    gridOptions

  // series 数据项
  let metricArr = []

  // 数据分组
  if (hasGroups && groups) {
    grouped = makeGourped(dataSource, [].concat(groups).filter(i => !!i))
  }

  sizeOptions = size && {
    symbolSize: function (data) {
      return Math.sqrt(data[0] * data[1]) / size
    }
  }
  shadowOptions = shadow && shadow.length && {
    shadowBlur: 10,
    shadowColor: 'rgba(0, 0, 0, 0.35)',
    shadowOffsetX: 10,
    shadowOffsetY: 10
  }
  labelOptions = label && {
    label: {
      emphasis: Object.assign({}, {
        show: true,
        opacity: 0.8,
        position: 'top',
        formatter: function (param) {
          return param.data[2]
        }
      }, shadowOptions)
    }
  }

  if (hasGroups && groups) {
    Object
      .keys(grouped)
      .forEach(k => {
        let serieObj = Object.assign({},
          {
            name: k,
            type: type,
            data: grouped[k].map(g => [g[xAxis], g[yAxis], g[label]])
          },
          sizeOptions,
          labelOptions
        )
        metricArr.push(serieObj)
      })
  } else {
    let serieObj = Object.assign({},
      {
        name: '数据',
        type: type,
        data: dataSource.map(g => [g[xAxis], g[yAxis], g[label]])
      },
      sizeOptions,
      labelOptions
    )
    metricArr.push(serieObj)
  }

  metricOptions = {
    series: metricArr
  }

  // 交叉轴
  splitLineOptions = splitLine && splitLine.length && {
    splitLine: {
      lineStyle: {
        type: 'dashed'
      }
    }
  }

  // x轴数据
  xAxisOptions = {
    xAxis: Object.assign({
      type: 'value'
    }, splitLineOptions)
  }

  yAxisOptions = {
    yAxis: Object.assign({
      type: 'value',
      scale: true
    }, splitLineOptions)
  }

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
    metricOptions,
    xAxisOptions,
    yAxisOptions,
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