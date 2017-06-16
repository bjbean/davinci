import { takeLatest } from 'redux-saga'
import { call, fork, put } from 'redux-saga/effects'

import { LOGIN } from './constants'
import { logged } from './actions'

import request from '../../utils/request'
import api from '../../utils/api'
import { notifySagasError } from '../../utils/util'
import { promiseSagaCreator } from '../../utils/reduxPromisation'
import { readListAdapter } from '../../utils/asyncAdapter'

export const login = promiseSagaCreator(
  function* ({ username, password }) {
    const asyncData = yield call(request, {
      method: 'post',
      url: api.login,
      data: {
        username,
        password
      }
    })
    const loginUser = readListAdapter(asyncData)
    yield put(logged(loginUser))
    localStorage.setItem('loginUser', JSON.stringify(loginUser))
    return loginUser
  },
  function (err) {
    notifySagasError(err, 'login')
  }
)

export function* loginWatcher () {
  yield fork(takeLatest, LOGIN, login)
}

export default [
  loginWatcher
]
