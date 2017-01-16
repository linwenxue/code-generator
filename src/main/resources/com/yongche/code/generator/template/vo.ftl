package ${package}.entity;

<#if comment??>
${comment}
</#if>
public class ${name} {
    <#list fields as field>
    <#if field.comment??>
    ${field.comment!}
    </#if>
    protected ${field.type} ${field.name};
    </#list>
 	
 	<#list fields as field>
    public ${field.type} get${field.name?cap_first}() {
        return ${field.name};
    }

	public void set${field.name?cap_first}(${field.type} ${field.name}) {
        this.${field.name} = ${field.name};
    }
    <#if field_has_next>

    </#if>
    </#list>
}