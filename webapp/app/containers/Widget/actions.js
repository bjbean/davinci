import {
  LOAD_WIDGETS,
  LOAD_WIDGETS_SUCCESS,
  ADD_WIDGET,
  ADD_WIDGET_SUCCESS,
  LOAD_WIDGET_DETAIL,
  LOAD_WIDGET_DETAIL_SUCCESS,
  EDIT_WIDGET,
  EDIT_WIDGET_SUCCESS,
  DELETE_WIDGET,
  DELETE_WIDGET_SUCCESS
} from './constants'

import { promiseActionCreator } from '../../utils/reduxPromisation'

export const loadWidgets = promiseActionCreator(LOAD_WIDGETS)

export const addWidget = promiseActionCreator(ADD_WIDGET, ['widget'])

export const loadWidgetDetail = promiseActionCreator(LOAD_WIDGET_DETAIL, ['id'])

export const editWidget = promiseActionCreator(EDIT_WIDGET, ['widget'])

export const deleteWidget = promiseActionCreator(DELETE_WIDGET, ['id'])

export function widgetsLoaded (widgets) {
  return {
    type: LOAD_WIDGETS_SUCCESS,
    payload: {
      widgets
    }
  }
}

export function widgetAdded (result) {
  return {
    type: ADD_WIDGET_SUCCESS,
    payload: {
      result
    }
  }
}

export function widgetDetailLoaded (widget) {
  return {
    type: LOAD_WIDGET_DETAIL_SUCCESS,
    payload: {
      widget
    }
  }
}

export function widgetEdited (result) {
  return {
    type: EDIT_WIDGET_SUCCESS,
    payload: {
      result
    }
  }
}

export function widgetDeleted (id) {
  return {
    type: DELETE_WIDGET_SUCCESS,
    payload: {
      id
    }
  }
}
