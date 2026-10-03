/**
 * Bounded Context ConsentManagement. Consents; anticorruption layer to the external consent tool.
 *
 * <ul>
 *   <li>{@code domain} - aggregates, value objects, domain events; no framework code</li>
 *   <li>{@code application} - use cases and ports</li>
 *   <li>{@code adapter.in} - REST (generated from the contract) and message consumers</li>
 *   <li>{@code adapter.out} - persistence, clients of upstream contexts, publishers</li>
 * </ul>
 */
package org.larder.consentmanagement;
