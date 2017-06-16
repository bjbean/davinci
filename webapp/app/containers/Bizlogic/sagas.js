import { takeLatest, takeEvery } from 'redux-saga'
import { call, fork, put } from 'redux-saga/effects'
import csvParser from 'jquery-csv'
import {
  LOAD_BIZLOGICS,
  ADD_BIZLOGIC,
  DELETE_BIZLOGIC,
  LOAD_BIZLOGIC_DETAIL,
  LOAD_BIZLOGIC_GROUPS,
  EDIT_BIZLOGIC,
  LOAD_BIZDATAS
} from './constants'
import {
  bizlogicsLoaded,
  bizlogicAdded,
  bizlogicDeleted,
  bizlogicDetailLoaded,
  bizlogicGroupsLoaded,
  bizlogicEdited,
  bizdatasLoaded
} from './actions'

import request from '../../utils/request'
import api from '../../utils/api'
import { notifySagasError } from '../../utils/util'
import { promiseSagaCreator } from '../../utils/reduxPromisation'
import { writeAdapter, readListAdapter, readObjectAdapter } from '../../utils/asyncAdapter'

export const getBizlogics = promiseSagaCreator(
  function* () {
    const asyncData = yield call(request, api.bizlogic)
    const bizlogics = readListAdapter(asyncData)
    yield put(bizlogicsLoaded(bizlogics))
    return bizlogics
  },
  function (err) {
    notifySagasError(err, 'getBizlogics')
  }
)

export function* getBizlogicsWatcher () {
  yield fork(takeLatest, LOAD_BIZLOGICS, getBizlogics)
}

export const addBizlogic = promiseSagaCreator(
  function* ({ bizlogic }) {
    const asyncData = yield call(request, {
      method: 'post',
      url: api.bizlogic,
      data: writeAdapter(bizlogic)
    })
    const result = readObjectAdapter(asyncData)
    yield put(bizlogicAdded(result))
    return result
  },
  function (err) {
    notifySagasError(err, 'addBizlogic')
  }
)

export function* addBizlogicWatcher () {
  yield fork(takeEvery, ADD_BIZLOGIC, addBizlogic)
}

export const deleteBizlogic = promiseSagaCreator(
  function* ({ id }) {
    yield call(request, {
      method: 'delete',
      url: `${api.bizlogic}/${id}`
    })
    yield put(bizlogicDeleted(id))
  },
  function (err) {
    notifySagasError(err, 'deleteBizlogic')
  }
)

export function* deleteBizlogicWatcher () {
  yield fork(takeEvery, DELETE_BIZLOGIC, deleteBizlogic)
}

export const getBizlogicDetail = promiseSagaCreator(
  function* ({ id }) {
    const bizlogic = yield call(request, `${api.bizlogic}/${id}`)
    yield put(bizlogicDetailLoaded(bizlogic))
    return bizlogic
  },
  function (err) {
    notifySagasError(err, 'getBizlogicDetail')
  }
)

export function* getBizlogicDetailWatcher () {
  yield fork(takeLatest, LOAD_BIZLOGIC_DETAIL, getBizlogicDetail)
}

export const getBizlogicGroups = promiseSagaCreator(
  function* ({ id }) {
    const asyncData = yield call(request, `${api.bizlogic}/${id}/groups`)
    const groups = readListAdapter(asyncData)
    yield put(bizlogicGroupsLoaded(groups))
    return groups
  },
  function (err) {
    notifySagasError(err, 'getBizlogicGroups')
  }
)

export function* getBizlogicGroupsWatcher () {
  yield fork(takeLatest, LOAD_BIZLOGIC_GROUPS, getBizlogicGroups)
}

export const editBizlogic = promiseSagaCreator(
  function* ({ bizlogic }) {
    yield call(request, {
      method: 'put',
      url: api.bizlogic,
      data: writeAdapter(bizlogic)
    })
    yield put(bizlogicEdited(bizlogic))
  },
  function (err) {
    notifySagasError(err, 'editBizlogic')
  }
)

export function* editBizlogicWatcher () {
  yield fork(takeEvery, EDIT_BIZLOGIC, editBizlogic)
}

export const getBizdatas = promiseSagaCreator(
  function* ({ id, sql }) {
    const asyncData = yield call(request, {
      method: 'post',
      url: `${api.bizlogic}/${id}/resultset`,
      data: sql || {}
    })
    const bizdatas = resultsetConverter(readListAdapter(asyncData))
    yield put(bizdatasLoaded(bizdatas))
    return bizdatas
  },
  function (err) {
    notifySagasError(err, 'getBizdatas')
  }
)

function resultsetConverter (resultset) {
  if (resultset.result && resultset.result.length) {
    const arr = resultset.result
    const keys = csvParser.toArray(arr.splice(0, 1)[0])
    return arr.map(csvVal => {
      const jsonVal = csvParser.toArray(csvVal)
      let obj = {}
      keys.forEach((k, index) => {
        obj[k] = jsonVal[index]
      })
      return obj
    })
  } else {
    return []
  }
}

export function* getBizdatasWatcher () {
  yield fork(takeEvery, LOAD_BIZDATAS, getBizdatas)
}

export default [
  getBizlogicsWatcher,
  addBizlogicWatcher,
  deleteBizlogicWatcher,
  getBizlogicDetailWatcher,
  getBizlogicGroupsWatcher,
  editBizlogicWatcher,
  getBizdatasWatcher
]
