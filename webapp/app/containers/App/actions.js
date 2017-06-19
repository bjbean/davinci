import {
  LOGIN,
  LOGGED,
  LOGOUT,
  SET_LOGIN_USER,
  GET_LOGIN_USER
} from './constants'

import { promiseActionCreator } from '../../utils/reduxPromisation'

export const login = promiseActionCreator(LOGIN, ['username', 'password'])

export const logout = promiseActionCreator(LOGOUT)

export const setLoginUser = promiseActionCreator(SET_LOGIN_USER, ['user'])

export const getLoginUser = promiseActionCreator(GET_LOGIN_USER)

export function logged (user) {
  return {
    type: LOGGED,
    payload: {
      user
    }
  }
}
