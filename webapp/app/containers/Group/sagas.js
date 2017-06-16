import { takeLatest, takeEvery } from 'redux-saga'
import { call, fork, put } from 'redux-saga/effects'
import {
  LOAD_GROUPS,
  ADD_GROUP,
  DELETE_GROUP,
  LOAD_GROUP_DETAIL,
  EDIT_GROUP
} from './constants'
import {
  groupsLoaded,
  groupAdded,
  groupDeleted,
  groupDetailLoaded,
  groupEdited
} from './actions'

import request from '../../utils/request'
import api from '../../utils/api'
import { notifySagasError } from '../../utils/util'
import { promiseSagaCreator } from '../../utils/reduxPromisation'
import { writeAdapter, readListAdapter, readObjectAdapter } from '../../utils/asyncAdapter'

export const getGroups = promiseSagaCreator(
  function* () {
    const asyncData = yield call(request, api.group)
    const groups = readListAdapter(asyncData)
    yield put(groupsLoaded(groups))
    return groups
  },
  function (err) {
    notifySagasError(err, 'getGroups')
  }
)

export function* getGroupsWatcher () {
  yield fork(takeLatest, LOAD_GROUPS, getGroups)
}

export const addGroup = promiseSagaCreator(
  function* ({ group }) {
    const asyncData = yield call(request, {
      method: 'post',
      url: api.group,
      data: writeAdapter(group)
    })
    const result = readObjectAdapter(asyncData)
    yield put(groupAdded(result))
    return result
  },
  function (err) {
    notifySagasError(err, 'addGroup')
  }
)

export function* addGroupWatcher () {
  yield fork(takeEvery, ADD_GROUP, addGroup)
}

export const deleteGroup = promiseSagaCreator(
  function* ({ id }) {
    yield call(request, {
      method: 'delete',
      url: `${api.group}/${id}`
    })
    yield put(groupDeleted(id))
  },
  function (err) {
    notifySagasError(err, 'deleteGroup')
  }
)

export function* deleteGroupWatcher () {
  yield fork(takeEvery, DELETE_GROUP, deleteGroup)
}

export const getGroupDetail = promiseSagaCreator(
  function* (payload) {
    const asyncData = yield call(request, `${api.group}/${payload.id}`)
    const group = readObjectAdapter(asyncData)
    yield put(groupDetailLoaded(group))
    return group
  },
  function (err) {
    notifySagasError(err, 'getGroupDetail')
  }
)

export function* getGroupDetailWatcher () {
  yield fork(takeLatest, LOAD_GROUP_DETAIL, getGroupDetail)
}

export const editGroup = promiseSagaCreator(
  function* ({ group }) {
    yield call(request, {
      method: 'put',
      url: api.group,
      data: writeAdapter(group)
    })
    yield put(groupEdited(group))
  },
  function (err) {
    notifySagasError(err, 'editGroup')
  }
)

export function* editGroupWatcher () {
  yield fork(takeEvery, EDIT_GROUP, editGroup)
}

export default [
  getGroupsWatcher,
  addGroupWatcher,
  deleteGroupWatcher,
  getGroupDetailWatcher,
  editGroupWatcher
]
