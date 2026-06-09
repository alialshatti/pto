<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" 
        xmlns:xs="http://www.w3.org/2001/XMLSchema" 
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
        xmlns:pxc="urn:peppol:xslt:custom-function" 
        queryBinding="xslt2">

    <title>Oman TDD Invoice Validation Rules</title>

    <ns prefix="cac" uri="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"/>
    <ns prefix="cbc" uri="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"/>
    <ns prefix="ext" uri="urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2"/>

    <pattern id="OM-TDD-Rules">
        <!-- Sample Rule 1: Supplier VAT is mandatory -->
        <rule context="/Invoice/cac:AccountingSupplierParty/cac:Party">
            <assert id="OM-BR-001" test="cac:PartyTaxScheme/cbc:CompanyID" flag="fatal">
                [OM-BR-001] Supplier VAT number is mandatory.
            </assert>
        </rule>
        
        <!-- Sample Rule 2: Invoice must have an ID -->
        <rule context="/Invoice">
            <assert id="OM-BR-002" test="cbc:ID" flag="fatal">
                [OM-BR-002] Invoice ID is mandatory.
            </assert>
        </rule>
    </pattern>

</schema>
