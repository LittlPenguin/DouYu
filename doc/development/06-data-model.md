# Data Model

## Android Model Target

Android models are Java POJOs. Kotlin data classes are removed from Android.

## Backend Model Target

Backend entity structure remains Java/Spring based. This stage removes runtime
demo content, not the domain tables.

## Empty Data Meaning

An empty posts/products/messages/patterns/assets result is normal after seed
removal. Empty does not mean the client should create sample rows.

## Seed/Demo Removal

The following runtime data categories are removed:

- Demo posts.
- Demo products and SKUs.
- Demo topics.
- Demo stickers.
- Demo/system content user used for filler.
- Static seed image URLs.

## Test Data

Tests must create explicit fixtures. Fixture names may be test-specific, but
production startup code must not create them.
