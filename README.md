# Multi-Brand Vehicle Feature Tile

## Key Features

- Clean Sling Model with a sensible injector strategy
- Accessibility-first HTL (semantic HTML, justified ARIA use, correct alt handling)
- A Granite dialog that's genuinely usable by an author
- Brand-aware theming without per-brand component forks
- TypeScript (not plain JS) for any client-side behavior

### Clean Sling Model with a sensible injector strategy

- Sling Model with annotations (@ValueMapValue)

### Accessibility-first HTL (semantic HTML, justified ARIA use, correct alt handling)

- use \<header>, \<nav>, \<section>, \<article>, etc
- use Site Audit for scanning
- \<article> over \<div>
  - identifies content for screen readers
  - designed for self-contained content
  - help search engine to understand the content
- for \<img>
  - alt="image description"
- for \<a href>
  - aria-label="xxxxx (opens in new tab)"
  - rel="noopener noreferrer"

### A Granite dialog that's genuinely usable by an author

- author-friendly UX
- fieldTitle and fieldDescription
- Use Tab
- Group similar fields in well
- Text place holder

### Brand-aware theming without per-brand component forks

The brand name recognition play the major factor of the project architecture.
The brand name can be derived by

#### Domain name, or URL

> #### Not a good idea.
>
> It can be re-written by CDN, or Web server.
>
> Difficult for the development testing

#### page properties

> #### Good idea.
>
> Increase the workload for the content author. The default value can be defined in the page initial content.
>
> Easy to mock for the development testing

#### Template Path

> #### Good idea.
>
> Create separated sites for each brand. Each brand has separated set of Editable Templates. Content Author pick the right template without making mistake.
>
> Easy to mock for the development testing

### TypeScript (not plain JS) for any client-side behavior

- webpack update

## ASSUMPTION:

- It is a Multi Brands Site and only support one language and one region (us-en)
- There is a master brand for default content and style
- Each brand has it's own SITE. The blueprint is coming from the master brand.

## Site and Template structure

        /conf/
        ├── brand-a/settings/wcm/templates
        ├── brand-b/settings/wcm/templates
        ├── brand-c/settings/wcm/templates
        └── brand-d/settings/wcm/templates

Part 2: Reusability Writeup
In your README (max one page), briefly explain:

1. How you'd keep four brands rendering from one component without per-brand forks
2. Where brand-specific CSS lives (clientlib structure)
3. What you would place in the shared core module vs. each brand's ui.apps

# Reusability Writeup

There will be one core component to handle four brands without using proxy component. The brand will be derived from the Page Template path. As long as the correct Editable Template under the correct SITE was picked, the targeted CSS style apply into the component.

There will be a global styles clientlibs. Each brand have it's own clientlibs. It include brand's CSS variables and styles. Also, we can assign brand clientlib into the Editable Template's policy.

## Components and Clientlibs structure

        /apps/client/
        ├── components/
        │   └── core/                      <-- Shared Logic & HTML
        │       └── vehicle-feature-tile/
        │           ├── vehicle-feature-tile.html        <-- HTL script
        │           └── .vehicle-feature-tile.xml        <-- component definition
        └── clientlibs/
            ├── clientlib-base/            <-- Global styles (reset, grid)
            ├── clientlib-brand-a/         <-- Brand A CSS variables & overrides
            └── clientlib-brand-b/         <-- Brand B CSS variables & overrides

