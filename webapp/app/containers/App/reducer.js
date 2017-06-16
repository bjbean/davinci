import {
  LOGIN,
  LOGGED,
  LOGOUT,
  SET_LOGIN_USER
} from './constants'
import { fromJS } from 'immutable'

const initialState = fromJS({
  logged: false,
  loginUser: null
})

function appReducer (state = initialState, { type, payload }) {
  switch (type) {
    case LOGIN:
      return state
    case LOGGED:
      return state
        .set('logged', true)
        .set('loginUser', payload.user)
    case LOGOUT:
      return state
        .set('logged', false)
        .set('loginUser', null)
    case SET_LOGIN_USER:
      return state
        .set('loginUser', payload.user)
    default:
      return state
  }
}

export default appReducer
