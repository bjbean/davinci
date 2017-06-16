import { fromJS } from 'immutable'

import {
  LOAD_DASHBOARDS_SUCCESS,
  ADD_DASHBOARD_SUCCESS,
  EDIT_DASHBOARD_SUCCESS,
  DELETE_DASHBOARD_SUCCESS,
  LOAD_DASHBOARD_DETAIL_SUCCESS,
  ADD_DASHBOARD_ITEM_SUCCESS,
  EDIT_DASHBOARD_ITEM_SUCCESS,
  EDIT_DASHBOARD_ITEMS_SUCCESS,
  DELETE_DASHBOARD_ITEM_SUCCESS,
  CLEAR_CURRENT_DASHBOARD
} from './constants'

const initialState = fromJS({
  dashboards: false,
  currentDashboard: null,
  currentItems: false
})

function dashboardReducer (state = initialState, { type, payload }) {
  let dashboards = state.get('dashboards')
  let items = state.get('currentItems')

  switch (type) {
    case LOAD_DASHBOARDS_SUCCESS:
      return state.set('dashboards', payload.dashboards)
    case ADD_DASHBOARD_SUCCESS:
      if (dashboards) {
        dashboards.unshift(payload.result)
        return state.set('dashboards', dashboards.slice())
      } else {
        return state.set('dashboards', [payload.result])
      }
    case EDIT_DASHBOARD_SUCCESS:
      dashboards.splice(dashboards.indexOf(dashboards.find(d => d.id === payload.result.id)), 1, payload.result)
      return state.set('dashboards', dashboards.slice())
    case DELETE_DASHBOARD_SUCCESS:
      return state.set('dashboards', dashboards.filter(d => d.id !== payload.id))
    case LOAD_DASHBOARD_DETAIL_SUCCESS:
      return state
        .set('currentDashboard', payload.dashboard)
        .set('currentItems', payload.dashboard.widgets)
    case ADD_DASHBOARD_ITEM_SUCCESS:
      if (items) {
        items.unshift(payload.result)
        return state.set('currentItems', items.slice())
      } else {
        return state.set('currentItems', [payload.result])
      }
    case EDIT_DASHBOARD_ITEM_SUCCESS:
      items.splice(items.indexOf(items.find(i => i.id === payload.result.id)), 1, payload.result)
      return state.set('currentItems', items.slice())
    case EDIT_DASHBOARD_ITEMS_SUCCESS:
      return state.set('currentItems', payload.result)
    case DELETE_DASHBOARD_ITEM_SUCCESS:
      return state.set('currentItems', items.filter(i => i.id !== payload.id))
    case CLEAR_CURRENT_DASHBOARD:
      return state
        .set('currentDashboard', null)
        .set('currentItems', false)
    default:
      return state
  }
}

export default dashboardReducer
