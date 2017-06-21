import {
  LOAD_BIZLOGICS_SUCCESS,
  ADD_BIZLOGIC_SUCCESS,
  DELETE_BIZLOGIC_SUCCESS,
  EDIT_BIZLOGIC_SUCCESS
} from './constants'
import { fromJS } from 'immutable'

const initialState = fromJS({
  bizlogics: false
})

function bizlogicReducer (state = initialState, { type, payload }) {
  const bizlogics = state.get('bizlogics')
  switch (type) {
    case LOAD_BIZLOGICS_SUCCESS:
      return state.set('bizlogics', payload.bizlogics)
    case ADD_BIZLOGIC_SUCCESS:
      if (bizlogics) {
        bizlogics.unshift(payload.result)
        return state.set('bizlogics', bizlogics.slice())
      } else {
        return state.set('bizlogics', [payload.result])
      }
    case DELETE_BIZLOGIC_SUCCESS:
      return state.set('bizlogics', bizlogics.filter(g => g.id !== payload.id))
    case EDIT_BIZLOGIC_SUCCESS:
      bizlogics.splice(bizlogics.indexOf(bizlogics.find(g => g.id === payload.result.id)), 1, payload.result)
      return state.set('bizlogics', bizlogics.slice())
    default:
      return state
  }
}

export default bizlogicReducer
