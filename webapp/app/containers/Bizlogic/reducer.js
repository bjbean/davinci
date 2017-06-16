import {
  LOAD_BIZLOGICS,
  LOAD_BIZLOGICS_SUCCESS,
  ADD_BIZLOGIC,
  ADD_BIZLOGIC_SUCCESS,
  DELETE_BIZLOGIC,
  DELETE_BIZLOGIC_SUCCESS,
  LOAD_BIZLOGIC_DETAIL,
  LOAD_BIZLOGIC_DETAIL_SUCCESS,
  EDIT_BIZLOGIC,
  EDIT_BIZLOGIC_SUCCESS,
  LOAD_BIZDATAS,
  LOAD_BIZDATAS_SUCCESS
} from './constants'
import { fromJS } from 'immutable'

const initialState = fromJS({
  bizlogics: false
})

function bizlogicReducer (state = initialState, { type, payload }) {
  const bizlogics = state.get('bizlogics')
  switch (type) {
    case LOAD_BIZLOGICS:
      return state
    case LOAD_BIZLOGICS_SUCCESS:
      return state.set('bizlogics', payload.bizlogics)
    case ADD_BIZLOGIC:
      return state
    case ADD_BIZLOGIC_SUCCESS:
      if (bizlogics) {
        bizlogics.unshift(payload.result)
        return state.set('bizlogics', bizlogics.slice())
      } else {
        return state.set('bizlogics', [payload.result])
      }
    case DELETE_BIZLOGIC:
      return state
    case DELETE_BIZLOGIC_SUCCESS:
      return state.set('bizlogics', bizlogics.filter(g => g.id !== payload.id))
    case LOAD_BIZLOGIC_DETAIL:
      return state
    case LOAD_BIZLOGIC_DETAIL_SUCCESS:
      return state
    case EDIT_BIZLOGIC:
      return state
    case EDIT_BIZLOGIC_SUCCESS:
      bizlogics.splice(bizlogics.indexOf(bizlogics.find(g => g.id === payload.result.id)), 1, payload.result)
      return state.set('bizlogics', bizlogics.slice())
    case LOAD_BIZDATAS:
      return state
    case LOAD_BIZDATAS_SUCCESS:
      return state
    default:
      return state
  }
}

export default bizlogicReducer
