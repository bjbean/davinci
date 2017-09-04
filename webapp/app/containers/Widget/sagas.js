import { takeLatest, takeEvery } from 'redux-saga'
import { call, fork, put } from 'redux-saga/effects'
import {
  LOAD_WIDGETS,
  ADD_WIDGET,
  DELETE_WIDGET,
  LOAD_WIDGET_DETAIL,
  EDIT_WIDGET
} from './constants'
import {
  widgetsLoaded,
  widgetAdded,
  widgetDeleted,
  widgetDetailLoaded,
  widgetEdited
} from './actions'

import request from '../../utils/request'
import api from '../../utils/api'
import { promiseSagaCreator } from '../../utils/reduxPromisation'
import { writeAdapter, readObjectAdapter, readListAdapter } from '../../utils/asyncAdapter'

export const getWidgets = promiseSagaCreator(
  function* () {
    const asyncData = yield call(request, api.widget)
    const widgets = readListAdapter(asyncData)
    yield put(widgetsLoaded(widgets))
    return widgets
  },
  function (err) {
    console.log('getWidgets', err)
  }
)

export function* getWidgetsWatcher () {
  yield fork(takeLatest, LOAD_WIDGETS, getWidgets)
}

export const addWidget = promiseSagaCreator(
  function* ({ widget }) {
    const asyncData = yield call(request, {
      method: 'post',
      url: api.widget,
      data: writeAdapter(widget)
    })
    const result = readObjectAdapter(asyncData)
    yield put(widgetAdded(result))
    return result
  },
  function (err) {
    console.log('addWidget', err)
  }
)

export function* addWidgetWatcher () {
  yield fork(takeEvery, ADD_WIDGET, addWidget)
}

export const deleteWidget = promiseSagaCreator(
  function* ({ id }) {
    yield call(request, {
      method: 'delete',
      url: `${api.widget}/${id}`
    })
    yield put(widgetDeleted(id))
  },
  function (err) {
    console.log('deleteWidget', err)
  }
)

export function* deleteWidgetWatcher () {
  yield fork(takeEvery, DELETE_WIDGET, deleteWidget)
}

export const getWidgetDetail = promiseSagaCreator(
  function* (payload) {
    const widget = yield call(request, `${api.widget}/${payload.id}`)
    yield put(widgetDetailLoaded(widget))
    return widget
  },
  function (err) {
    console.log('getWidgetDetail', err)
  }
)

export function* getWidgetDetailWatcher () {
  yield fork(takeLatest, LOAD_WIDGET_DETAIL, getWidgetDetail)
}

export const editWidget = promiseSagaCreator(
  function* ({ widget }) {
    yield call(request, {
      method: 'put',
      url: api.widget,
      data: writeAdapter(widget)
    })
    yield put(widgetEdited(widget))
  },
  function (err) {
    console.log('editWidget', err)
  }
)

export function* editWidgetWatcher () {
  yield fork(takeEvery, EDIT_WIDGET, editWidget)
}

export default [
  getWidgetsWatcher,
  addWidgetWatcher,
  deleteWidgetWatcher,
  getWidgetDetailWatcher,
  editWidgetWatcher
]
