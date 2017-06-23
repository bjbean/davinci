export function initializePosition (loginUser, dashboard, items) {
  if (!loginUser.admin) {
    const posInStorage = localStorage.getItem(`${loginUser.id}_${dashboard.id}_position`)
    if (posInStorage) {
      const localPos = JSON.parse(posInStorage)
      return items.map(i => {
        const itemInLocal = localPos.find(p => p.i === `${i.id}`)
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
          return Object.assign({}, itemInLocal)
        }
      })
    }
  }
  return items.map(i => ({
    x: i.position_x,
    y: i.position_y,
    w: i.width,
    h: i.length,
    i: `${i.id}`,
    widget_id: i.widget_id
  }))
}

export function changePosition (prev, current, rerender) {
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
  return prev
}

export function diffPosition (origin, current) {
  let sign = false
  for (let i = 0, cl = current.length; i < cl; i += 1) {
    const oItem = origin[i]
    const cItem = current[i]
    if (oItem.x !== cItem.x ||
        oItem.y !== cItem.y ||
        oItem.w !== cItem.w ||
        oItem.h !== cItem.h) {
      sign = true
      break
    }
  }
  return sign
}
