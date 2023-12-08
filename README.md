# Поисковый движок сайта
Поисковый движок помогает посетителям быстро найти информацию на определённый сайтах.
# Screenshots
![Dashboard](https://user-images.githubusercontent.com/87513274/139628698-caec41c7-1edf-4857-be5f-2d12adf76406.PNG)
![Management](https://user-images.githubusercontent.com/87513274/139628891-0424c020-6b34-4adb-8c0b-76d6ab31fcdf.PNG)
![Search](https://user-images.githubusercontent.com/87513274/139628957-71fef9ef-29f0-4807-b562-52d1f3112081.PNG)
# Принципы работы
1. В конфигурационном файле перед запуском приложения задаются адреса сайтов, по которым движок должен осуществлять поиск.
2. Поисковый движок должен самостоятельно обходить все страницы заданных сайтов и индексировать их (создавать так называемый индекс) так,
   чтобы потом находить наиболее релевантные страницы по любому поисковому запросу.
3. Пользователь присылает запрос через API движка. Запрос — это набор слов, по которым нужно найти страницы сайта.
4. Запрос определённым образом трансформируется в список слов,переведённых в базовую форму.
    Например, для существительных — именительный падеж, единственное число.
5. В индексе ищутся страницы, на которых встречаются все эти слова
6. Результаты поиска ранжируются, сортируются и отдаются пользователю.
# Как запустить проект
## Зависимости
Для успешного скачивания и подключения к проекту зависимостей из GitHub необходимо настроить Maven конфигурацию в файле `settings.xml`.
А зависимостях, в файле `pom.xml` добавлен репозиторий для получения jar файлов:
```
<repositories>
  <repository>
    <id>skillbox-gitlab</id>
    <url>https://gitlab.skillbox.ru/api/v4/projects/263574/packages/maven</url>
  </repository>
</repositories>
```
Так как для доступа требуется авторизации по токену для получения данных из публичного репозитория, для указания токена, найдите файл `settings.xml`.
- В Windows он располагается в директории `C:/Users/<Имя вашего пользователя>/.m2`
- В Linux директория `/home/<Имя вашего пользователя>/.m2`
- В macOs по адресу `/Users/<Имя вашего пользователя>/.m2`
> Внимание! Актуальный токен, строка которую надо вставить в тег `<value>...</value>` [находится в документе по ссылке](https://docs.google.com/document/d/1rb0ysFBLQltgLTvmh-ebaZfJSI7VwlFlEYT9V5_aPjc/edit).
и добавьте внутри тега `settings` текст конфигурации:
```
<servers>
  <server>
    <id>skillbox-gitlab</id>
    <configuration>
      <httpHeaders>
        <property>
          <name>Private-Token</name>
          <value>token</value>
        </property>
      </httpHeaders>
    </configuration>
  </server>
</servers>
```
Не забудьте поменять токен на актуальный!
Если файла нет, то создайте `settings.xml` и вставьте в него:
```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
 https://maven.apache.org/xsd/settings-1.0.0.xsd">

  <servers>
    <server>
      <id>skillbox-gitlab</id>
      <configuration>
        <httpHeaders>
          <property>
            <name>Private-Token</name>
            <value>token</value>
          </property>
        </httpHeaders>
      </configuration>
    </server>
  </servers>

</settings>
```
Не забудьте поменять токен на актуальный!
После этого, в проекте обновите зависимости (Ctrl+Shift+O / ⌘⇧I) или принудительно обновите данные из pom.xml.
Для этого вызовите контекстное у файла `pom.xml` в дереве файла проектов Project и выберите пункт меню Maven -> Reload Project.
Если после этого у вас остается ошибка:
```
Could not transfer artifact org.apache.lucene.morphology:morph:pom:1.5
from/to gitlab-skillbox (https://gitlab.skillbox.ru/api/v4/projects/263574/packages/maven):
authentication failed for
https://gitlab.skillbox.ru/api/v4/projects/263574/packages/maven/russianmorphology/org/apache/
    lucene/morphology/morph/1.5/morph-1.5.pom,
status: 401 Unauthorized
```
Почистите кэш Maven. Самый надежный способ, удалить директорию:
- Windows `C:\Users\<user_name>\.m2\repository`
- macOs `/Users/<user_name>/.m2/repository`
- Linux `/home/<user_name>/.m2/repository`
где `<user_name>` - имя пользователя под которым вы работаете.
После этого снова попробуйте обновить данный из `pom.xml`
## Настройки подключения к БД
В проект добавлен драйвер для подключения к БД MySQL. Для запуска проекта, убедитесь, что у вас запущен сервер MySQL 8.x.
В MySQL создайте базу данных с названием `search_engine`.
Замените логин и пароль в файле конфигурации, который лежит в корне проекта `application.yml`:
```
spring:
  datasource:
    username: root # имя пользователя
    password: Kimk7FjT # пароль пользователя
```
После этого, можете запустить проект.
## Замена сайтов поиска на свой/свои сайты
Что бы запустить индексацию своего/своих сайтов необходимо в том же файле конфигурации `application.yml` добавить свои настройки: `url` - url своего сайта, `name` - название сайта(можете придумать самостоятельно):
```
indexing-settings:
  sites:
    - url: http://srpp63.ru/
      name: Самарское Речное Пассажирское Предприятие
    - url: http://www.playback.ru/
      name: Смартфоны
```
Стоит удалить БД `search_engine` в MySQL, и создать заново, что бы старые данные не влияли на работу приложения.
После этих действий нужно просто перезапустить проект.
Стоит иметь ввиду, что в текущей конфигурации сайты небольшие, их индексация не займёт много времени. Так же там нет защиты от поисковых машин, чего не скажешь о многих других сайтах.
