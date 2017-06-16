import { LOGIN, LOGGED, LOGOUT, SET_LOGIN_USER } from './constants'

import { promiseActionCreator } from '../../utils/reduxPromisation'

export const login = promiseActionCreator(LOGIN, ['username', 'password'])

export const logout = promiseActionCreator(LOGOUT)

export const setLoginUser = promiseActionCreator(SET_LOGIN_USER, ['user'])

export function logged (user) {
  return {
    type: LOGGED,
    payload: {
      user
    }
  }
}
