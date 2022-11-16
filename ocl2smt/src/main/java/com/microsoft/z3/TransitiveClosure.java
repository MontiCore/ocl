package com.microsoft.z3;

public class TransitiveClosure {
  public static <R extends Sort> FuncDecl<R> mkTransitiveClosure(Context ctx, FuncDecl<R> f) {
    // checkContextMatch(f);
    return new FuncDecl<>(ctx, Native.mkTransitiveClosure(ctx.nCtx(), f.getNativeObject()));
  }
}
