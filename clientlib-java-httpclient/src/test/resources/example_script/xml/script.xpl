<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="foo:script" version="1.0"
                xmlns:foo="http://www.daisy.org/pipeline/modules/foo/"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<h1 px:role="name">Example script</h1>
	</p:documentation>
	
	<p:input port="source" px:name="source" px:media-type="application/x-dtbook+xml"/>
	
	<p:option name="option-1" required="true" px:type="string" px:data-type="foo:choice"/>
	
	<p:option name="option-2" required="false" px:type="string" px:data-type="foo:regex" select="'one'"/>
	
	<p:sink/>
	
</p:declare-step>
