package ru.georgeee.itmo.sem6.dkvs.utils;

import lombok.Getter;

/**
 * Simple type-strict helper class for value, able to have either type A or type B
 * It's written so, that value with type Either<A, B> can take only null, instance of Left<A, B> or instance of Right<A, B>.
 * Common pattern for using the class:
 * <pre>
 *     {@code
 * void processLeft(A a);
 * void processRight(B b);
 * void processEither(Either<A, B> either){
 *   if (either != null) {
 *      if (either instanceof Either.Left) {
 *          processLeft(either.getLeft());
 *      } else {
 *          processRight(either.getRight());
 *      }
 *   }
 * }
 * }
 * </pre>
 * Inspired by Either from Haskell.
 */
public abstract class Either<A, B> {
    private Either() {
    }

    public A getLeft() {
        throw new UnsupportedOperationException();
    }

    public B getRight() {
        throw new UnsupportedOperationException();
    }

    public static final class Left<A, B> extends Either<A, B> {

        @Getter
        private final A left;

        public Left(A left) {
            this.left = left;
        }

        @Override
        public String toString() {
            return "Ether.Left{" +
                    "left=" + left +
                    '}';
        }
    }

    public static final class Right<A, B> extends Either<A, B> {
        @Getter
        private final B right;

        public Right(B right) {
            this.right = right;
        }

        @Override
        public String toString() {
            return "Either.Right{" +
                    "right=" + right +
                    '}';
        }
    }
}
