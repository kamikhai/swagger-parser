<#ftl output_format="plainText">
import 'package:retrofit/retrofit.dart';
import 'package:dio/dio.dart';
<#if objects??>
<#list objects as item>
import '${item}';
</#list>
</#if>

part '${file_name}.g.dart';

@RestApi(baseUrl: "https:${base_url}")
abstract class ${client_name}Client {
factory ${client_name}Client(Dio dio, {String baseUrl}) = _${client_name}Client;

<#if methods??>
    <#list methods as item>
    @${item.operation}("${item.path}")
    ${item.returnType} ${item.methodName}(<#if item.parameters??><#list item.parameters as param>${param}, </#list></#if>);

    </#list>
</#if>
}