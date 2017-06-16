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

import { promiseActionCreator } from '../../utils/reduxPromisation'

export const loadSources = promiseActionCreator(LOAD_SOURCES)

export const addSource = promiseActionCreator(ADD_SOURCE, ['source'])

export const deleteSource = promiseActionCreator(DELETE_SOURCE, ['id'])

export const loadSourceDetail = promiseActionCreator(LOAD_SOURCE_DETAIL, ['id'])

export const editSource = promiseActionCreator(EDIT_SOURCE, ['source'])

export function sourcesLoaded (sources) {
  return {
    type: LOAD_SOURCES_SUCCESS,
    payload: {
      sources
    }
  }
}

export function sourceAdded (result) {
  return {
    type: ADD_SOURCE_SUCCESS,
    payload: {
      result
    }
  }
}

export function sourceDeleted (id) {
  return {
    type: DELETE_SOURCE_SUCCESS,
    payload: {
      id
    }
  }
}

export function sourceDetailLoaded (source) {
  return {
    type: LOAD_SOURCE_DETAIL_SUCCESS,
    payload: {
      source
    }
  }
}

export function sourceEdited (result) {
  return {
    type: EDIT_SOURCE_SUCCESS,
    payload: {
      result
    }
  }
}
