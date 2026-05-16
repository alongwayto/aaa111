export function toLineSeries(labels = [], values = [], name = '数据') {
  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value' },
    series: [{ name, type: 'line', smooth: true, data: values }],
  }
}

export function toPieSeries(items = []) {
  return {
    tooltip: { trigger: 'item' },
    series: [{ type: 'pie', radius: ['40%', '68%'], data: items }],
  }
}
