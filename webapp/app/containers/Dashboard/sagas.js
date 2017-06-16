import { takeLatest, takeEvery } from 'redux-saga'
import { call, fork, put } from 'redux-saga/effects'
import {
  LOAD_DASHBOARDS,
  ADD_DASHBOARD,
  EDIT_DASHBOARD,
  DELETE_DASHBOARD,
  LOAD_DASHBOARD_DETAIL,
  ADD_DASHBOARD_ITEM,
  EDIT_DASHBOARD_ITEM,
  EDIT_DASHBOARD_ITEMS,
  DELETE_DASHBOARD_ITEM
} from './constants'

import {
  dashboardsLoaded,
  dashboardAdded,
  dashboardEdited,
  dashboardDeleted,
  dashboardDetailLoaded,
  dashboardItemAdded,
  dashboardItemEdited,
  dashboardItemsEdited,
  dashboardItemDeleted
} from './actions'

import request from '../../utils/request'
import api from '../../utils/api'
import { notifySagasError } from '../../utils/util'
import { promiseSagaCreator } from '../../utils/reduxPromisation'
import { writeAdapter, readObjectAdapter, readListAdapter } from '../../utils/asyncAdapter'

export const getDashboards = promiseSagaCreator(
  function* () {
    const asyncData = yield call(request, api.dashboard)
    const dashboards = readListAdapter(asyncData)
    yield put(dashboardsLoaded(dashboards))
    return dashboards
  },
  function (err) {
    notifySagasError(err, 'getDashboards')
  }
)

export function* getDashboardsWatcher () {
  yield fork(takeLatest, LOAD_DASHBOARDS, getDashboards)
}

export const addDashboard = promiseSagaCreator(
  function* ({ dashboard }) {
    const asyncData = yield call(request, {
      method: 'post',
      url: api.dashboard,
      data: writeAdapter(dashboard)
    })
    const result = readObjectAdapter(asyncData)
    yield put(dashboardAdded(result))
    return result
  },
  function (err) {
    notifySagasError(err, 'addDashboard')
  }
)

export function* addDashboardWatcher () {
  yield fork(takeEvery, ADD_DASHBOARD, addDashboard)
}

export const editDashboard = promiseSagaCreator(
  function* ({ dashboard }) {
    yield call(request, {
      method: 'put',
      url: api.dashboard,
      data: writeAdapter(dashboard)
    })
    yield put(dashboardEdited(dashboard))
  },
  function (err) {
    notifySagasError(err, 'editDashboard')
  }
)

export function* editDashboardWatcher () {
  yield fork(takeEvery, EDIT_DASHBOARD, editDashboard)
}

export const deleteDashboard = promiseSagaCreator(
  function* ({ id }) {
    yield call(request, {
      method: 'delete',
      url: `${api.dashboard}/${id}`
    })
    yield put(dashboardDeleted(id))
  },
  function (err) {
    notifySagasError(err, 'deleteDashboard')
  }
)

export function* deleteDashboardWatcher () {
  yield fork(takeEvery, DELETE_DASHBOARD, deleteDashboard)
}

export const getDashboardDetail = promiseSagaCreator(
  function* ({ id }) {
    const asyncData = yield call(request, `${api.dashboard}/${id}`)
    const dashboard = readListAdapter(asyncData) // FIXME 返回格式不标准
    yield put(dashboardDetailLoaded(dashboard))
    return dashboard
  },
  function (err) {
    notifySagasError(err, 'getDashboardDetail')
  }
)

export function* getDashboardDetailWatcher () {
  yield fork(takeLatest, LOAD_DASHBOARD_DETAIL, getDashboardDetail)
}

export const addDashboardItem = promiseSagaCreator(
  function* ({ item }) {
    const asyncData = yield call(request, {
      method: 'post',
      url: `${api.dashboard}/widgets`,
      data: writeAdapter(item)
    })
    const result = readObjectAdapter(asyncData)
    yield put(dashboardItemAdded(result))
    return result
  },
  function (err) {
    notifySagasError(err, 'addDashboardItem')
  }
)

export function* addDashboardItemWatcher () {
  yield fork(takeEvery, ADD_DASHBOARD_ITEM, addDashboardItem)
}

export const editDashboardItem = promiseSagaCreator(
  function* ({ item }) {
    yield call(request, {
      method: 'put',
      url: `${api.dashboard}/widgets`,
      data: writeAdapter(item)
    })
    yield put(dashboardItemEdited(item))
  },
  function (err) {
    notifySagasError(err, 'editDashboardItem')
  }
)

export function* editDashboardItemWatcher () {
  yield fork(takeEvery, EDIT_DASHBOARD_ITEM, editDashboardItem)
}

export const editDashboardItems = promiseSagaCreator(
  function* ({ items }) {
    yield call(request, {
      method: 'put',
      url: `${api.dashboard}/widgets`,
      data: {
        payload: items
      }
    })
    yield put(dashboardItemsEdited(items))
  },
  function (err) {
    notifySagasError(err, 'editDashboardItems')
  }
)

export function* editDashboardItemsWatcher () {
  yield fork(takeEvery, EDIT_DASHBOARD_ITEMS, editDashboardItems)
}

export const deleteDashboardItem = promiseSagaCreator(
  function* ({ id }) {
    yield call(request, {
      method: 'delete',
      url: `${api.dashboard}/widgets/${id}`
    })
    yield put(dashboardItemDeleted(id))
  },
  function (err) {
    notifySagasError(err, 'deleteDashboardItem')
  }
)

export function* deleteDashboardItemWatcher () {
  yield fork(takeEvery, DELETE_DASHBOARD_ITEM, deleteDashboardItem)
}

export default [
  getDashboardsWatcher,
  addDashboardWatcher,
  editDashboardWatcher,
  deleteDashboardWatcher,
  getDashboardDetailWatcher,
  addDashboardItemWatcher,
  editDashboardItemWatcher,
  editDashboardItemsWatcher,
  deleteDashboardItemWatcher
]
