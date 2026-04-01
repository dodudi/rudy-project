# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Development (requires PostgreSQL running)
npm run dev

# Build
npm run build

# Start built app
npm run start

# Lint
npm run lint

# Prisma: apply migrations to local DB
npx prisma migrate dev --name <migration-name>

# Prisma: regenerate client after schema changes
npx prisma generate

# Prisma: open DB browser
npx prisma studio

# Docker: build and run full stack
docker compose up --build -d

# Docker: stop
docker compose down
```

`.env` must contain `DATABASE_URL=postgresql://...` for local development. The Docker Compose default is `postgresql://rudy:rudy1234@db:5432/blog`.

## Architecture

**Stack**: Next.js 16.2 App Router · React 19 · Tailwind CSS v4 · Prisma v5 · PostgreSQL · `@milkdown/crepe` v7

### File structure

```
src/
├── app/
│   ├── layout.tsx                  # RootLayout (wraps Header)
│   ├── page.tsx                    # Home — loads all posts, renders PostFeed
│   ├── globals.css                 # Tailwind v4 + Milkdown overrides
│   ├── post/[id]/
│   │   ├── page.tsx                # Fetches post by id, renders PostDetailWrapper
│   │   └── PostDetailWrapper.tsx   # dynamic-imports PostDetail (ssr: false)
│   ├── write/
│   │   └── page.tsx                # Fetches draft + categories, renders WriteForm
│   └── settings/categories/
│       └── page.tsx                # Fetches categories, renders CategoryManager
├── components/
│   ├── layout/Header.tsx
│   ├── post/
│   │   ├── PostFeed.tsx            # Client: search/category/tag filter via useMemo
│   │   ├── PostList.tsx            # Renders list of PostCard
│   │   ├── PostCard.tsx            # Card with title, summary, tags, reading time
│   │   └── PostDetail.tsx          # Detail view + in-place edit mode (no Draft)
│   ├── editor/
│   │   ├── MilkdownEditor.tsx      # Milkdown Crepe wrapper (ssr: false)
│   │   ├── WriteForm.tsx           # New post form with auto-save draft
│   │   └── DraftBanner.tsx         # Banner shown on home when draft exists
│   ├── filter/
│   │   ├── SearchBar.tsx
│   │   ├── CategoryFilter.tsx
│   │   └── TagFilter.tsx
│   ├── settings/CategoryManager.tsx
│   └── ui/
│       ├── Button.tsx              # primary / secondary / danger variants
│       └── TagBadge.tsx
├── lib/
│   ├── db/index.ts                 # Prisma Client singleton (dev hot-reload safe)
│   ├── actions/
│   │   ├── posts.ts                # createPost, updatePostInPlace, deletePost
│   │   ├── draft.ts                # saveDraft, deleteDraft
│   │   └── categories.ts           # addCategory, deleteCategory
│   ├── readingTime.ts              # readingTime(content), summarize(content)
│   └── highlight.ts                # getHighlightParts(text, query) → yellow-200
└── types/index.ts                  # Post, Draft, Category interfaces
```

### Data model

Three Prisma models: `Post`, `Draft`, `Category`.

- `Post.category` stores the category **name as a plain string** (not a FK). When a `Category` is deleted, posts are updated to `category = ""` via `prisma.post.updateMany`.
- `Draft` is a **single-row table** with a hardcoded `id = "draft"`. Saved via `upsert`, deleted via `deleteMany`. Only used during new post creation (not editing existing posts).
- `Post.tags` and `Draft.tags` are `String[]` (Postgres array). After fetching from Prisma, `createdAt`/`updatedAt` must be converted to ISO strings before passing to client components.
- `Post.image` and `Draft.image` are optional strings (stored as DataURL from file input).

### Pages and data flow

All DB-fetching pages require `export const dynamic = 'force-dynamic'` to prevent static rendering failures at build time.

Filtering (search/category/tag) is done **client-side** in `PostFeed`: all posts are loaded server-side on the home page, then filtered in-memory via `useMemo`. This is intentional for a personal blog scale.

### Server Actions

Located in `src/lib/actions/`. All actions call `revalidatePath('/')` to invalidate cache.

- `createPost` and `deletePost` use `redirect()` from `next/navigation` — do not return values.
- `updatePostInPlace` does **not** redirect — it only revalidates, and the caller does `router.refresh()`.
- `saveDraft` and `deleteDraft` do not redirect.
- `addCategory` and `deleteCategory` do not redirect.

### Milkdown editor

`MilkdownEditor` (`src/components/editor/MilkdownEditor.tsx`) must be loaded with `next/dynamic` and `{ ssr: false }`. Its CSS (`@milkdown/crepe/theme/common/style.css` and `frame.css`) is imported **inside the component file**, not in `globals.css` — the Turbopack bundler resolves package-relative CSS imports reliably from JS module context but not from CSS `@import` chains.

The editor mounts via `useEffect` and the cleanup must call both `crepe.destroy()` and `container.innerHTML = ''` to handle React StrictMode's double-mount.

The editor `key` prop in `WriteForm` forces a remount when switching contexts, since `defaultValue` is only read at mount time.

`globals.css` contains Milkdown overrides: `.milkdown-wrap .ProseMirror` (padding/min-height), `.milkdown-readonly` (read-only mode), and hides block handles via `display: none !important`.

### Draft auto-save

`WriteForm` auto-saves to Draft with a **2-second debounce** on every field change. `PostDetail` (edit mode) does **not** use Draft — it writes directly to the Post via `updatePostInPlace`.

### Styling

Tailwind v4 syntax — `globals.css` uses `@import "tailwindcss"` and `@plugin "@tailwindcss/typography"`. There is no `tailwind.config.js`. The typography plugin is activated via `prose` classes on post content.

### Docker deployment

`next.config.ts` sets `output: 'standalone'`. The Dockerfile uses a multi-stage build (deps → builder → runner) and copies the standalone output plus Prisma binaries from `node_modules/.prisma`, `node_modules/@prisma`, and `node_modules/prisma`. On container startup, `prisma migrate deploy` runs before `node server.js`.

### Prisma version constraint

Prisma **v5** is required. Prisma v7 removed `url = env("DATABASE_URL")` from the schema datasource block and broke the configuration. Do not upgrade to v7.
