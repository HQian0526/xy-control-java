##### 【项目简介】
**项目初衷**：为多种智能化设备控制场景提供一套完整、成熟、稳定的解决方案；
**系统适配场景**：无人自习室、无人棋牌室、智能家居、智能停车场等；
**系统使用情景**：客户通过微信小程序预定门店订单，到店后系统凭订单授予客户特定的设备控制权限（如门店门禁、特定位置的灯控等），实现自助预定门店订单、自助体验门店服务、自助控制门店设备，最大化降低人力成本，进而实现小成本创业。
##### 【作者简介】
本人打工人程序猿一枚，毕业至今已有四五载，因厌倦了一眼望不到头的职场牛马生涯，遂萌生创业/搞副业想法，一来给自己留条退路，哪天被裁或者真辞职了不至于一点收入没有；二来万一副业成功了呢是吧。
##### 【本地部署】
该系统分为前端小程序代码+后端代码+mysql数据库库表结构，缺一不可。本项目仅为后端代码部分。需要其他部分实现本地部署可联系作者邮箱：963497073@qq.com。
##### 【系统框架】
springboot + mybatis-plus + mysql
java.version：21
##### 【代码规范】
1、项目中的实体名、方法名等均要按照驼峰命名法命名；
2、entity文件夹：实体类，实体命名必须驼峰命名，若数据库表中字段有下划线等应在XXXMapper.xml中配置好映射关系（参考resource/mapper/UserMapper.xml）。实体类使用maven的lombok插件的注解自动生成，不要手动写getter/setter以及有参/无参构造函数；
3、接口-增：接口命名为“add+实体类名字”，例如addUser。
4、接口-改：接口命名为“update+实体类名字”，例如updateUser。
5、接口-查：接口命名为“find+实体类名字”，例如findUser，所有查询接口均应实现模糊分页查询，可参考resource/mapper/UserMapper.xml。
6、接口-删：接口命名为“delete+实体类名字”，例如deleteUser。项目中所有的删除接口均为逻辑删除。删除接口不能自己手写sql，使用mybatis-plus提供的逻辑删除方法，在applicaton.yml中配好逻辑删除字段后，调用mybatis-plus提供的deleteBatchIds方法来实现删除。