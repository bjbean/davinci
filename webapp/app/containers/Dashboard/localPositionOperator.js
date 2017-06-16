export function initializePosition (loginUser, dashboardId, items) {
  const positionsInStorage = localStorage.getItem(`${loginUser}_${dashboardId}_position`)
  let positions = null

  if (positionsInStorage) {
    const oldPos = JSON.parse(positionsInStorage)
    positions = items.map(i => {
      const itemInLocal = oldPos.find(p => p.i === `${i.id}`)
      if (!itemInLocal) {
        return {
          x: i.position_x,
          y: i.position_y,
          w: i.width,
          h: i.length,
          i: `${i.id}`,
          widget_id: i.widget_id
        }
      } else {
        return Object.assign({}, itemInLocal, { widget_id: i.widget_id })
      }
    })
  } else {
    positions = items.map(i => ({
      x: i.position_x,
      y: i.position_y,
      w: i.width,
      h: i.length,
      i: `${i.id}`,
      widget_id: i.widget_id
    }))
  }
  localStorage.setItem(`${loginUser}_${dashboardId}_position`, JSON.stringify(positions))
  return positions
}

export function changePosition (loginUser, dashboardId, prev, current, rerender) {
  current.forEach((item, index) => {
    const prevItem = prev[index]
    prevItem.x = item.x
    prevItem.y = item.y
    if (prevItem.w !== item.w || prevItem.h !== item.h) {
      rerender(prevItem)
      prevItem.w = item.w
      prevItem.h = item.h
    }
  })
  localStorage.setItem(`${loginUser}_${dashboardId}_position`, JSON.stringify(prev))
  return prev
}

export function diffPosition (origin, current) {
  let sign = false
  for (let i = 0, cl = current.length; i < cl; i += 1) {
    const oItem = origin[i]
    const cItem = current[i]
    if (oItem.position_x !== cItem.x ||
        oItem.position_y !== cItem.y ||
        oItem.width !== cItem.w ||
        oItem.length !== cItem.h) {
      sign = true
      break
    }
  }
  return sign
}
