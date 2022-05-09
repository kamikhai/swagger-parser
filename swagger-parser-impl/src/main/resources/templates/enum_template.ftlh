<#ftl output_format="plainText">
import 'package:json_annotation/json_annotation.dart';

part '${file_name}.g.dart';

@JsonEnum(alwaysCreate: true)
enum ${class_name} {
    <#if values??>
    <#list values as item>
    @JsonValue("${item}") ${item},
    </#list>
    </#if>
}