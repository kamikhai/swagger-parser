<#ftl output_format="plainText">
import 'package:json_annotation/json_annotation.dart';
<#if objects??>
<#list objects as item>
import '${item}';
</#list>
</#if>

part '${file_name}.g.dart';

@JsonSerializable(genericArgumentFactories: true)
class ${class_name}<T> {
    ${class_name}(
    <#if fields?? && fields?has_content>{
        <#list fields as item>
            <#if item.required>required </#if>this.${item.name},
        </#list>
        }
    </#if>
    );

<#if fields??>
    <#list fields as item>
    ${item.type}<#if !item.required>?</#if> ${item.name};
    </#list>
</#if>

    factory ${class_name}.fromJson(Map<String, dynamic> json, T Function(Object? json) fromJsonT) => _$${class_name}FromJson(json, fromJsonT);

    Map<String, dynamic> toJson(Object Function(T value) toJsonT) => _$${class_name}ToJson(this, toJsonT);
}