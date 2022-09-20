package gremlin.scala

import scala.annotation.implicitNotFound

sealed trait FeatureFlag
class FeatureEnabled extends FeatureFlag
class FeatureDisabled extends FeatureFlag


@implicitNotFound("No given instance of MarshallingFeatureSet has been imported, try `import gremlin.scala.MarshallingFeatureSet.Neptune.given` or `import gremlin.scala.MarshallingFeatureSet.FullMonty.given`")
trait MarshallingFeatureSet {
  type AnyValFlag <: FeatureFlag
  type CaseClassFlag <: FeatureFlag
  type SeqFlag <: FeatureFlag
  type SetFlag <: FeatureFlag
  type MapFlag <: FeatureFlag
}

object MarshallingFeatureSet {
  object PrimitivesOnly:
    transparent inline given MarshallingFeatureSet =
      new MarshallingFeatureSet {
        type AnyValFlag = FeatureDisabled
        type CaseClassFlag = FeatureDisabled
        type SeqFlag = FeatureDisabled
        type SetFlag = FeatureDisabled
        type MapFlag = FeatureDisabled
      }

  object FullMonty:
    transparent inline given MarshallingFeatureSet =
      new MarshallingFeatureSet {
        type AnyValFlag = FeatureEnabled
        type CaseClassFlag = FeatureEnabled
        type SeqFlag = FeatureEnabled
        type SetFlag = FeatureEnabled
        type MapFlag = FeatureEnabled
      }

  object Neptune:
    transparent inline given MarshallingFeatureSet =
      new MarshallingFeatureSet {
        type AnyValFlag = FeatureEnabled
        type CaseClassFlag = FeatureDisabled
        type SeqFlag = FeatureDisabled
        type SetFlag = FeatureDisabled
        type MapFlag = FeatureDisabled
      }

  object Testing:
    transparent inline given MarshallingFeatureSet =
      new MarshallingFeatureSet {
        type AnyValFlag = FeatureEnabled
        type CaseClassFlag = FeatureDisabled
        type SeqFlag = FeatureEnabled
        type SetFlag = FeatureDisabled
        type MapFlag = FeatureDisabled
      }

}

