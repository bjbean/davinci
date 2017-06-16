import { LOAD_SIDEBAR } from './constants'
import { fromJS } from 'immutable'

const initialState = fromJS({
  sidebar: false
})

function reportReducer (state = initialState, action) {
  switch (action.type) {
    case LOAD_SIDEBAR:
      return state.set('sidebar', action.sidebar)
    default:
      return state
  }
}

export default reportReducer
