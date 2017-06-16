import { takeEvery, takeLatest } from 'redux-saga'
import { call, fork, put } from 'redux-saga/effects'
import {
  LOAD_USERS,
  ADD_USER,
  DELETE_USER,
  LOAD_USER_DETAIL,
  LOAD_USER_GROUPS,
  EDIT_USER_INFO,
  CHANGE_USER_PASSWORD
} from './constants'
import {
  usersLoaded,
  userAdded,
  userDeleted,
  userDetailLoaded,
  userGroupsLoaded,
  userInfoEdited,
  userPasswordChanged
} from './actions'

import request from '../../utils/request'
import api from '../../utils/api'
import { notifySagasError } from '../../utils/util'
import { promiseSagaCreator } from '../../utils/reduxPromisation'
import { writeAdapter, readObjectAdapter, readListAdapter } from '../../utils/asyncAdapter'

export const getUsers = promiseSagaCreator(
  function* () {
    const asyncData = yield call(request, api.user)
    const users = readListAdapter(asyncData)
    yield put(usersLoaded(users))
    return users
  },
  function (err) {
    notifySagasError(err, 'getUsers')
  }
)

export function* getUsersWatcher () {
  yield fork(takeLatest, LOAD_USERS, getUsers)
}

export const addUser = promiseSagaCreator(
  function* ({ user }) {
    const asyncData = yield call(request, {
      method: 'post',
      url: api.user,
      data: writeAdapter(user)
    })
    const result = readObjectAdapter(asyncData)
    yield put(userAdded(result))
    return result
  },
  function (err) {
    notifySagasError(err, 'addUser')
  }
)

export function* addUserWatcher () {
  yield fork(takeEvery, ADD_USER, addUser)
}

export const deleteUser = promiseSagaCreator(
  function* ({ id }) {
    yield call(request, {
      method: 'delete',
      url: `${api.user}/${id}`
    })
    yield put(userDeleted(id))
  },
  function (err) {
    notifySagasError(err, 'deleteUser')
  }
)

export function* deleteUserWatcher () {
  yield fork(takeEvery, DELETE_USER, deleteUser)
}

export const getUserDetail = promiseSagaCreator(
  function* ({ id }) {
    const user = yield call(request, `${api.user}/${id}`)
    yield put(userDetailLoaded(user))
    return user
  },
  function (err) {
    notifySagasError(err, 'getUserDetail')
  }
)

export function* getUserDetailWatcher () {
  yield fork(takeLatest, LOAD_USER_DETAIL, getUserDetail)
}

export const getUserGroups = promiseSagaCreator(
  function* ({ id }) {
    const asyncData = yield call(request, `${api.user}/${id}/groups`)
    const groups = readListAdapter(asyncData)
    yield put(userGroupsLoaded(groups))
    return groups
  },
  function (err) {
    notifySagasError(err, 'getUserGroups')
  }
)

export function* getUserGroupsWatcher () {
  yield fork(takeLatest, LOAD_USER_GROUPS, getUserGroups)
}

export const editUserInfo = promiseSagaCreator(
  function* ({ user }) {
    yield call(request, {
      method: 'put',
      url: api.user,
      data: writeAdapter(user)
    })
    yield put(userInfoEdited(user))
  },
  function (err) {
    notifySagasError(err, 'editUserInfo')
  }
)

export function* editUserInfoWatcher () {
  yield fork(takeEvery, EDIT_USER_INFO, editUserInfo)
}

export const changeUserPassword = promiseSagaCreator(
  function* ({ info }) {
    yield call(request, {
      method: 'post',
      url: `${api.changepwd}/users`,
      data: info
        // writeAdapter(info)
    })
    yield put(userPasswordChanged())
  },
  function (err) {
    notifySagasError(err, 'changeUserPassword')
  }
)

export function* changeUserPasswordWatcher () {
  yield fork(takeEvery, CHANGE_USER_PASSWORD, changeUserPassword)
}

export default [
  getUsersWatcher,
  addUserWatcher,
  deleteUserWatcher,
  getUserDetailWatcher,
  getUserGroupsWatcher,
  editUserInfoWatcher,
  changeUserPasswordWatcher
]
