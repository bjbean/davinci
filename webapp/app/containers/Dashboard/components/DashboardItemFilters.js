import React, { PropTypes, PureComponent } from 'react'
import classnames from 'classnames'

import Form from 'antd/lib/form'
import Input from 'antd/lib/input'
import Select from 'antd/lib/select'
import Button from 'antd/lib/button'
import Icon from 'antd/lib/icon'
const Option = Select.Option
const FormItem = Form.Item

import { uuid } from '../../../utils/util'

import styles from '../Dashboard.less'

export class DashboardItemFilters extends PureComponent {
  constructor (props) {
    super(props)
    this.state = {
      // filterTree: localStorage.getItem(`${props.loginUser.id}_${props.itemId}_filterTree`)
      filterTree: {},
      flattenTree: null
    }
  }

  renderFilterList = (filter, items) => {
    const { getFieldDecorator } = this.props.form
    const itemClass = classnames({
      [styles.filterItem]: true,
      [styles.noPadding]: true,
      [styles.root]: filter.root
    })

    return (
      <div key={filter.id} className={itemClass}>
        <div className={styles.filterBlock}>
          <div className={styles.filterRel}>
            <FormItem className={styles.filterFormItem}>
              {getFieldDecorator(`${filter.id}Rel`, {
                initialValue: filter.rel
              })(
                <Select>
                  <Option value="and">And</Option>
                  <Option value="or">Or</Option>
                </Select>
              )}
            </FormItem>
          </div>
          <list className={styles.filterList}>
            {items}
          </list>
        </div>
      </div>
    )
  }

  renderFilterItem = (filter) => {
    const { getFieldDecorator } = this.props.form
    const itemClass = classnames({
      [styles.filterItem]: true,
      [styles.root]: filter.root
    })

    const forkButton = filter.root || (
      <Button shape="circle" icon="fork" type="primary" onClick={this.forkNode(filter.id)} />
    )

    const operators = ['=', 'like', '>', '<', '>=', '<=', '!=']

    const keySelectItems = this.props.keys.map(k => (
      <Option key={k} value={k}>{k}</Option>
    ))

    const operatorSelectItems = operators.map(k => (
      <Option key={k} value={k}>{k}</Option>
    ))

    return (
      <div key={filter.id} className={itemClass}>
        <FormItem className={`${styles.filterFormItem} ${styles.filterFormKey}`}>
          {getFieldDecorator(`${filter.id}KeySelect`, {
            initialValue: filter.filterKey
          })(
            <Select placeholder="Column" onSelect={this.changeFilterKey(filter)}>
              {keySelectItems}
            </Select>
          )}
        </FormItem>
        <FormItem className={`${styles.filterFormItem} ${styles.filterFormOperator}`}>
          {getFieldDecorator(`${filter.id}OperatorSelect`, {
            initialValue: filter.filterType
          })(
            <Select onSelect={this.changeFilterTypes(filter)}>
              {operatorSelectItems}
            </Select>
          )}
        </FormItem>
        <FormItem className={styles.filterFormItem}>
          {getFieldDecorator(`${filter.id}Input`, {
            initialValue: filter.filterValue
          })(
            <Input onChange={this.changeFilterValue(filter)} />
          )}
        </FormItem>
        <Button shape="circle" icon="plus" type="primary" onClick={this.addParallelNode(filter.id)} />
        {forkButton}
        <Button shape="circle" icon="minus" onClick={this.deleteNode(filter.id)} />
      </div>
    )
  }

  renderFilters (filter) {
    if (filter.type === 'link') {
      const items = filter.children.map(c => this.renderFilters(c))
      return this.renderFilterList(filter, items)
    } else if (filter.type === 'node') {
      return this.renderFilterItem(filter)
    } else {
      return (
        <div className={`${styles.filterForm} ${styles.empty}`} onClick={this.addTreeRoot}>
          <h3>
            <Icon type="plus" /> 点击添加查询条件
          </h3>
        </div>
      )
    }
  }

  addTreeRoot = () => {
    const rootId = uuid(8, 16)
    const root = {
      id: rootId,
      root: true,
      type: 'node'
    }
    this.setState({
      filterTree: root,
      flattenTree: {
        [rootId]: root
      }
    })
  }

  addParallelNode = (nodeId) => () => {
    const { flattenTree } = this.state

    let currentNode = flattenTree[nodeId]
    let newNode = {
      id: uuid(8, 16),
      type: 'node'
    }

    if (currentNode.parent) {
      let parent = flattenTree[currentNode.parent]
      newNode.parent = parent.id
      parent.children.push(newNode)
      flattenTree[newNode.id] = newNode

      this.setState({
        flattenTree: Object.assign({}, flattenTree)
      })
    } else {
      let parent = {
        id: uuid(8, 16),
        root: true,
        type: 'link',
        rel: 'and',
        children: []
      }

      newNode.parent = parent.id
      parent.children.push(currentNode)
      parent.children.push(newNode)

      delete currentNode.root
      delete flattenTree[currentNode.id]
      currentNode.id = uuid(8, 16)
      currentNode.parent = parent.id

      flattenTree[currentNode.id] = currentNode
      flattenTree[parent.id] = parent
      flattenTree[newNode.id] = newNode

      this.setState({
        filterTree: parent,
        flattenTree: Object.assign({}, flattenTree)
      })
    }
  }

  forkNode = (nodeId) => () => {
    const { flattenTree } = this.state

    let currentNode = flattenTree[nodeId]
    let cloneNode = Object.assign({}, currentNode, {
      id: uuid(8, 16),
      parent: currentNode.id
    })
    let newNode = {
      id: uuid(8, 16),
      type: 'node',
      parent: currentNode.id
    }

    currentNode = Object.assign(currentNode, {
      type: 'link',
      rel: 'and',
      children: [cloneNode, newNode]
    })

    flattenTree[cloneNode.id] = cloneNode
    flattenTree[newNode.id] = newNode

    this.setState({
      flattenTree: Object.assign({}, flattenTree)
    })
  }

  deleteNode = (nodeId) => () => {
    const { flattenTree } = this.state

    let currentNode = flattenTree[nodeId]
    delete flattenTree[nodeId]

    if (currentNode.parent) {
      let parent = flattenTree[currentNode.parent]
      parent.children = parent.children.filter(c => c.id !== nodeId)

      if (parent.children.length === 1) {
        let onlyChild = parent.children[0]
        this.refreshTreeId(onlyChild)

        parent = Object.assign(parent, {
          id: onlyChild.id,
          type: onlyChild.type,
          rel: onlyChild.rel,
          filterKey: onlyChild.filterKey,
          filterType: onlyChild.filterType,
          filterValue: onlyChild.filterValue,
          children: onlyChild.children
        })

        delete flattenTree[parent.id]
        flattenTree[onlyChild.id] = parent
      }

      this.setState({
        flattenTree: Object.assign({}, flattenTree)
      })
    } else {
      this.setState({
        filterTree: {},
        flattenTree: {}
      })
    }
  }

  refreshTreeId = (treeNode) => {
    const { flattenTree } = this.state
    const oldId = treeNode.id
    delete flattenTree[oldId]

    treeNode.id = uuid(8, 16)
    flattenTree[treeNode.id] = treeNode

    if (treeNode.children) {
      treeNode.children.forEach(c => {
        c.parent = treeNode.id
        this.refreshTreeId(c)
      })
    }
  }

  changeFilterKey = (filter) => (val) => {
    filter.filterKey = val
  }

  changeFilterTypes = (filter) => (val) => {
    filter.filterType = val
  }

  changeFilterValue = (filter) => (event) => {
    filter.filterValue = event.target.value
  }

  render () {
    const {
      filterTree
    } = this.state

    return (
      <Form className={styles.filterForm}>
        {this.renderFilters(filterTree)}
      </Form>
    )
  }
}

DashboardItemFilters.propTypes = {
  form: PropTypes.any,
  keys: PropTypes.array,
  types: PropTypes.array,
  loginUser: PropTypes.object,
  itemId: PropTypes.number
}

export default Form.create({withRef: true})(DashboardItemFilters)
