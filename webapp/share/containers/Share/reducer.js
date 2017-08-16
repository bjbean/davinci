import { fromJS } from 'immutable'

import {
  LOAD_SHARE_DASHBOARD_SUCCESS,
  SET_INDIVIDUAL_DASHBOARD
} from './constants'

const initialState = fromJS({
  items: false
})

function shareReducer (state = initialState, { type, payload }) {
  switch (type) {
    case LOAD_SHARE_DASHBOARD_SUCCESS:
      return state.set('items', payload.dashboard.widgets)
    case SET_INDIVIDUAL_DASHBOARD:
      return state.set('items', [{
        id: 1,
        position_x: 0,
        position_y: 0,
        width: 12,
        length: 10,
        trigger_type: 'manual',
        trigger_params: '',
        widget_id: payload.widgetId,
        aesStr: payload.token
      }])
    default:
      return state
  }
}

export default shareReducer
