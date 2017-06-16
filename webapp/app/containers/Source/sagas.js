import { takeLatest, takeEvery } from 'redux-saga'
import { call, fork, put } from 'redux-saga/effects'
import {
  LOAD_SOURCES,
  ADD_SOURCE,
  DELETE_SOURCE,
  LOAD_SOURCE_DETAIL,
  EDIT_SOURCE
} from './constants'
import {
  sourcesLoaded,
  sourceAdded,
  sourceDeleted,
  sourceDetailLoaded,
  sourceEdited
} from './actions'

import request from '../../utils/request'
import api from '../../utils/api'
import { notifySagasError } from '../../utils/util'
import { promiseSagaCreator } from '../../utils/reduxPromisation'
import { writeAdapter, readObjectAdapter, readListAdapter } from '../../utils/asyncAdapter'

export const getSources = promiseSagaCreator(
  function* () {
    const asyncData = yield call(request, api.source)
    const sources = readListAdapter(asyncData)
    yield put(sourcesLoaded(sources))
    return sources
  },
  function (err) {
    notifySagasError(err, 'getSources')
  }
)

export function* getSourcesWatcher () {
  yield fork(takeLatest, LOAD_SOURCES, getSources)
}

export const addSource = promiseSagaCreator(
  function* ({ source }) {
    const asyncData = yield call(request, {
      method: 'post',
      url: api.source,
      data: writeAdapter(source)
    })
    const result = readObjectAdapter(asyncData)
    yield put(sourceAdded(result))
    return result
  },
  function (err) {
    notifySagasError(err, 'addSource')
  }
)

export function* addSourceWatcher () {
  yield fork(takeEvery, ADD_SOURCE, addSource)
}

export const deleteSource = promiseSagaCreator(
  function* ({ id }) {
    yield call(request, {
      method: 'delete',
      url: `${api.source}/${id}`
    })
    yield put(sourceDeleted(id))
  },
  function (err) {
    notifySagasError(err, 'deleteSource')
  }
)

export function* deleteSourceWatcher () {
  yield fork(takeEvery, DELETE_SOURCE, deleteSource)
}

export const getSourceDetail = promiseSagaCreator(
  function* ({ id }) {
    const source = yield call(request, `${api.source}/${id}`)
    yield put(sourceDetailLoaded(source))
    return source
  },
  function (err) {
    notifySagasError(err, 'getSourceDetail')
  }
)

export function* getSourceDetailWatcher () {
  yield fork(takeLatest, LOAD_SOURCE_DETAIL, getSourceDetail)
}

export const editSource = promiseSagaCreator(
  function* ({ source }) {
    yield call(request, {
      method: 'put',
      url: api.source,
      data: writeAdapter(source)
    })
    yield put(sourceEdited(source))
  },
  function (err) {
    notifySagasError(err, 'editSource')
  }
)

export function* editSourceWatcher () {
  yield fork(takeEvery, EDIT_SOURCE, editSource)
}

export default [
  getSourcesWatcher,
  addSourceWatcher,
  deleteSourceWatcher,
  getSourceDetailWatcher,
  editSourceWatcher
]
