

# 自动更新系统 

插件框架自带了一套插件升级系统，可以根据宿主app自身需求，来下载/安装/更新插件。它可以根据插件版本、宿主版本、系统版本、插件框架版本等信息，按照自定义的规则进行选择性的更新升级。完全可以满足各种兼容性版本的控制需求。

插件更新系统通过一个update.xml文件来描述所有插件的更新信息、版本和条件规则等。

#  使用步骤

- **配置文件** - 将update.xml移动到files/plugin/config/目录，update.xml一般情况下由应用自行下载或者同步。

- **启动更新** - 调用PluginManager.getInstance().startUpdate()启动更新，更新系统将启动一个独立进程对update.xml进行解析和下载升级。

#  update.xml配置

[update.xml](../update/update.xml)

以下为一个标准的update.xml配置，一般情况下比这个简单，这里是为了提供一个比较全面的例子。下面对相关属性进行说明，未特意说明的属性项均为保留的非必填项。


```xml
<?xml version="1.0" encoding="utf-8"?>

<plugins> 

    <plugin tag="novel"> 
        <name>小说插件</name>  
        <packageName>cn.qihoo.reader</packageName>  
        <desc>360小说阅读器</desc> 
    </plugin>  

    <plugin tag="safebarcode"> 
        <name>扫码插件</name>  
        <packageName>cn.qihoo.barcode</packageName>  
        <desc>360安全扫码</desc> 
    </plugin>

    <plugin tag="map"> 
        <name>导航插件</name>  
        <packageName>com.qihoo.msearch.qmap</packageName>  
        <desc>360导航</desc> 
    </plugin>  

    <update tag="novel" version="1.0.2">
        <desc>修复了50个BUG</desc>
        <md5>142A0DF92AE5AC9869D5A299993FE420</md5>
        <force>true</force>
        <icon>http://m.360.cn/msearch/plugin/icon/reader_1_0_0.jpg</icon>
        <url>http://down.360safe.com/reader/360reader_android_beta.apk<url>
        <rules>
            <rule type="app" minVer="210" maxVer="500"/>
            <rule type="android" ignoreVers="21"/>
        </rules>
    </update>
	
    <update tag="map" version="1.0.1">
        <desc>修复了200个BUG，加快了启动速度</desc>
        <md5>CD8BBBBBC2009F7AB8B9E9D3781B76F7</md5>
        <force>true</force>
        <icon>http://m.360.cn/msearch/plugin/icon/barcode.jpg</icon>
        <url>http://down.360safe.com/360msearch/app/plugin/qihoo_plugin_MMapPlugin.apk</url>
        <rules>
        </rules>
    </update>
	
    <update tag="safebarcode" version="1.0.2">
        <desc>修复了200个BUG，加快了启动速度</desc>
        <md5>BABF695DD77ED96A967C6274626871D1</md5>
        <force>true</force>
        <icon>http://m.360.cn/msearch/plugin/icon/barcode.jpg</icon>
        <url>http://down.360safe.com/360msearch/app/plugin/qihoo_plugin_barcode.apk</url>
        <rules>
            <rule type="android" minVer="9" maxVer="21"/>
            <rule type="app" ignoreVers="213" />
            <rule type="host" minVer="1.0.3" maxVer="1.0.8" />
            <rule type="host" ignoreVers="1.0.7" />
        </rules>
    </update>
</plugins>

```

##   plugin节点
    
定义插件的基本信息，tag为插件的唯一标识号，必填，也可以填写包名。
    
    

##  update节点
    
定义了一个插件升级包，与plugin可以是多对一的关系，因为update节点中有规则，只有符合规则的情况下才会升级插件，update节点可以为同一个插件配置不同规则，这样可以控制不同的客户端/设备可以下载不同版本的插件。

- **version**

    必填项，为3位数字，格式为"xxx.xxx.xxx"，该版本号与apk本身的版本没有任何关系，属于给插件专门定义的版本号，在插件管理和更新管理中都以这个版本号为准。当更新系统检测到本地插件版本比version小时，说明需要更新该update所描述的更新包。
    
- **md5**
 
    必填项，为插件apk的32位纯大写MD5，在插件下载完成后，会验证该值，如果验证失败，则更新也会失败。

- **url**

    必填项，定义了插件的下载地址
    
- **rules**

    定义了一系列更新规则(rule)，可以没有也可以一条或多条。在满足了version的条件前提下，所有的规则(rule)均满足时才会下载并更新插件。
    
- **rule**

    - **type属性**

        - "app" 表示宿主程序，值使用app的versionCode
        - "host"表示插件容器，值为插件框架sdk内部的一个版本字符串
        - "android" 表示android系统版本，值为系统sdk的code
    
    - **匹配规则**
        - "minVer" 与 "maxVer" 定义了版本范围，当 minVer<=当前版本<=maxVer时，满足条件
        - "vers" 定义了随机版本号，类似于白名单，逗号分隔，满足其中的一个即满足规则
        - "ignoreVers" 定义了忽略版本号，类似于黑名单，逗号分隔，满足了其中一个规则不满足
    - **例子**
    
        <rule type="app" minVer="100" maxVer="200" \/>
        
        表示只有应用(宿主程序)的versionCode在100-200之间(包括100,200)，才升级该插件




## 完整实例

```xml

<?xml version="1.0" encoding="utf-8"?>

<plugins> 
    <plugin tag="myplugin"> 
        <name>测试插件</name>  
        <packageName>com.test.plugin</packageName>  
        <desc>我是一个测试插件</desc> 
    </plugin>
        
    <update tag="myplugin" version="1.0.1">
        <desc>修复了200个BUG，加快了启动速度</desc>
        <md5>CD8BBBBBC2009F7AB8B9E9D3781B76F7</md5>
        <url>http://down.360safe.com/plugin/myplugin.apk</url>
        <rules/>
    </update>
</plugins> 
  
```
以上为升级/新增一个插件的最简配置，如果匹配成功的插件在本地是没有安装的状态，则直接安装之。
