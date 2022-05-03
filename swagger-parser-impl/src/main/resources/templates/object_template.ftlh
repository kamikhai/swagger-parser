<#ftl output_format="plainText">
import 'package:json_annotation/json_annotation.dart';
<#if objects??>
<#list objects as item>
import '${item}.dart';
</#list>
</#if>

part '${file_name}.g.dart';

@JsonSerializable()
class ${class_name} {
${class_name}({
<#if fields??>
    <#list fields as item>
    <#if item.required>required </#if>this.${item.name},
    </#list>
</#if>
});

<#if fields??>
    <#list fields as item>
    ${item.type}<#if !item.required>?</#if> ${item.name};
    </#list>
</#if>

factory ${class_name}.fromJson(Map
<String, dynamic> json) => _$${class_name}FromJson(json);

Map
<String, dynamic> toJson() => _$${class_name}ToJson(this);
}