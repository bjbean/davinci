import {
  LOAD_SOURCES,
  LOAD_SOURCES_SUCCESS,
  ADD_SOURCE,
  ADD_SOURCE_SUCCESS,
  DELETE_SOURCE,
  DELETE_SOURCE_SUCCESS,
  LOAD_SOURCE_DETAIL,
  LOAD_SOURCE_DETAIL_SUCCESS,
  EDIT_SOURCE,
  EDIT_SOURCE_SUCCESS
} from './constants'
import { fromJS } from 'immutable'

const initialState = fromJS({
  sources: false
})

function sourceReducer (state = initialState, { type, payload }) {
  const sources = state.get('sources')
  switch (type) {
    case LOAD_SOURCES:
      return state
    case LOAD_SOURCES_SUCCESS:
      return state.set('sources', payload.sources)
    case ADD_SOURCE:
      return state
    case ADD_SOURCE_SUCCESS:
      if (sources) {
        sources.unshift(payload.result)
        return state.set('sources', sources.slice())
      } else {
        return state.set('sources', [payload.result])
      }
    case DELETE_SOURCE:
      return state
    case DELETE_SOURCE_SUCCESS:
      return state.set('sources', sources.filter(g => g.id !== payload.id))
    case LOAD_SOURCE_DETAIL:
      return state
    case LOAD_SOURCE_DETAIL_SUCCESS:
      return state
    case EDIT_SOURCE:
      return state
    case EDIT_SOURCE_SUCCESS:
      sources.splice(sources.indexOf(sources.find(g => g.id === payload.result.id)), 1, payload.result)
      return state.set('sources', sources.slice())
    default:
      return state
  }
}

export default sourceReducer
