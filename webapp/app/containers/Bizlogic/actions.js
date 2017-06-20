import {
  LOAD_BIZLOGICS,
  LOAD_BIZLOGICS_SUCCESS,
  ADD_BIZLOGIC,
  ADD_BIZLOGIC_SUCCESS,
  DELETE_BIZLOGIC,
  DELETE_BIZLOGIC_SUCCESS,
  LOAD_BIZLOGIC_DETAIL,
  LOAD_BIZLOGIC_DETAIL_SUCCESS,
  LOAD_BIZLOGIC_GROUPS,
  LOAD_BIZLOGIC_GROUPS_SUCCESS,
  EDIT_BIZLOGIC,
  EDIT_BIZLOGIC_SUCCESS,
  LOAD_BIZDATAS,
  LOAD_BIZDATAS_SUCCESS
} from './constants'

import { promiseActionCreator } from '../../utils/reduxPromisation'

export const loadBizlogics = promiseActionCreator(LOAD_BIZLOGICS)

export const addBizlogic = promiseActionCreator(ADD_BIZLOGIC, ['bizlogic'])

export const deleteBizlogic = promiseActionCreator(DELETE_BIZLOGIC, ['id'])

export const loadBizlogicDetail = promiseActionCreator(LOAD_BIZLOGIC_DETAIL, ['id'])

export const loadBizlogicGroups = promiseActionCreator(LOAD_BIZLOGIC_GROUPS, ['id'])

export const editBizlogic = promiseActionCreator(EDIT_BIZLOGIC, ['bizlogic'])

export const loadBizdatas = promiseActionCreator(LOAD_BIZDATAS, ['id', 'sql', 'offset', 'limit'])

export function bizlogicsLoaded (bizlogics) {
  return {
    type: LOAD_BIZLOGICS_SUCCESS,
    payload: {
      bizlogics
    }
  }
}

export function bizlogicAdded (result) {
  return {
    type: ADD_BIZLOGIC_SUCCESS,
    payload: {
      result
    }
  }
}

export function bizlogicDeleted (id) {
  return {
    type: DELETE_BIZLOGIC_SUCCESS,
    payload: {
      id
    }
  }
}

export function bizlogicDetailLoaded (bizlogic) {
  return {
    type: LOAD_BIZLOGIC_DETAIL_SUCCESS,
    payload: {
      bizlogic
    }
  }
}

export function bizlogicGroupsLoaded (groups) {
  return {
    type: LOAD_BIZLOGIC_GROUPS_SUCCESS,
    payload: {
      groups
    }
  }
}

export function bizlogicEdited (result) {
  return {
    type: EDIT_BIZLOGIC_SUCCESS,
    payload: {
      result
    }
  }
}

export function bizdatasLoaded (bizdatas) {
  return {
    type: LOAD_BIZDATAS_SUCCESS,
    payload: {
      bizdatas
    }
  }
}
