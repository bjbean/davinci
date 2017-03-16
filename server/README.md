# root url

https://davinci.creditease.corp/api/v1/domains/(domain_id)

# sub url

- /dashboards (POST/DELETE/PUT/GET(by all/id/filter))
- /widgets (POST/DELETE/PUT/GET(by all/id/filter))
- /bizlogics (POST/DELETE/PUT/GET(by all/id/filter))
- /users (POST/DELETE/PUT/GET(by all/id/filter))
all   /users
id    /users/(id) 
filter /users?sortby=name&order=asc


- /groups (POST/DELETE/PUT/GET(by all/id/filter))
- /settings (POST/DELETE/PUT/GET(by all/id/filter))
- /bindings/dashboards_widgets (POST)
- /bindings/groups_bizlogics (POST)
- /bindings/users_groups (POST)

# example

GET /zoos: List all Zoos (ID and Name, not too much detail)
POST /zoos: Create a new Zoo
GET /zoos/ZID: Retrieve an entire Zoo object
PUT /zoos/ZID: Update a Zoo (entire object)
PATCH /zoos/ZID: Update a Zoo (partial object)
DELETE /zoos/ZID: Delete a Zoo
GET /zoos/ZID/animals: Retrieve a listing of Animals (ID and Name).
GET /animals: List all Animals (ID and Name).
POST /animals: Create a new Animal
GET /animals/AID: Retrieve an Animal object
PUT /animals/AID: Update an Animal (entire object)
PATCH /animals/AID: Update an Animal (partial object)
GET /animal_types: Retrieve a listing (ID and Name) of all Animal Types
GET /animal_types/ATID: Retrieve an entire Animal Type object
GET /employees: Retrieve an entire list of Employees
GET /employees/EID: Retreive a specific Employee
GET /zoos/ZID/employees: Retrieve a listing of Employees (ID and Name) who work at this Zoo
POST /employees: Create a new Employee
POST /zoos/ZID/employees: Hire an Employee at a specific Zoo
DELETE /zoos/ZID/employees/EID: Fire an Employee from a specific Zoo

?limit=10: Reduce the number of results returned to the Consumer (for Pagination)
?offset=10: Send sets of information to the Consumer (for Pagination)
?animal_type_id=1: Filter records which match the following condition (WHERE animal_type_id = 1)
?sortby=name&order=asc: Sort the results based on the specified attribute (ORDER BY name ASC)

# authentication url
POST https://davinci.creditease.corp/api/v1/login
POST https://davinci.creditease.corp/api/v1/changepwd
POST https://davinci.creditease.corp/api/v1/logout


GET /zoos：列出所有动物园
POST /zoos：新建一个动物园
GET /zoos/ID：获取某个指定动物园的信息
PUT /zoos/ID：更新某个指定动物园的信息（提供该动物园的全部信息）
PATCH /zoos/ID：更新某个指定动物园的信息（提供该动物园的部分信息）
DELETE /zoos/ID：删除某个动物园
GET /zoos/ID/animals：列出某个指定动物园的所有动物
DELETE /zoos/ID/animals/ID：删除某个指定动物园的指定动物

?limit=10：指定返回记录的数量
?offset=10：指定返回记录的开始位置。
?page=2&per_page=100：指定第几页，以及每页的记录数。
?sortby=name&order=asc：指定返回结果按照哪个属性排序，以及排序顺序。
?animal_type_id=1：指定筛选条件


???批量删除
